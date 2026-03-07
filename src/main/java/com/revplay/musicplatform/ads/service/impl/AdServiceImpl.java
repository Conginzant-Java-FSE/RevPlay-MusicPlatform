package com.revplay.musicplatform.ads.service.impl;

import com.revplay.musicplatform.ads.entity.Ad;
import com.revplay.musicplatform.ads.entity.AdImpression;
import com.revplay.musicplatform.ads.entity.UserAdPlaybackState;
import com.revplay.musicplatform.ads.repository.AdImpressionRepository;
import com.revplay.musicplatform.ads.repository.AdRepository;
import com.revplay.musicplatform.ads.repository.UserAdPlaybackStateRepository;
import com.revplay.musicplatform.ads.service.AdService;
import com.revplay.musicplatform.exception.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdServiceImpl implements AdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdServiceImpl.class);

    private final AdRepository adRepository;
    private final AdImpressionRepository adImpressionRepository;
    private final UserAdPlaybackStateRepository userAdPlaybackStateRepository;

    public AdServiceImpl(
            AdRepository adRepository,
            AdImpressionRepository adImpressionRepository,
            UserAdPlaybackStateRepository userAdPlaybackStateRepository
    ) {
        this.adRepository = adRepository;
        this.adImpressionRepository = adImpressionRepository;
        this.userAdPlaybackStateRepository = userAdPlaybackStateRepository;
    }

    @Override
    @Transactional
    public Ad getNextAd(Long userId, Long songId) {
        if (userId == null || songId == null) {
            throw new BadRequestException("userId and songId are required");
        }

        UserAdPlaybackState state = userAdPlaybackStateRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserAdPlaybackState newState = new UserAdPlaybackState();
                    newState.setUserId(userId);
                    newState.setSongsPlayedCount(0);
                    return newState;
                });

        int updatedCount = (state.getSongsPlayedCount() == null ? 0 : state.getSongsPlayedCount()) + 1;
        state.setSongsPlayedCount(updatedCount);
        userAdPlaybackStateRepository.save(state);

        if (updatedCount % 3 != 0) {
            LOGGER.debug("No ad scheduled for userId={}, songsPlayedCount={}", userId, updatedCount);
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Ad> activeAds = adRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);
        if (activeAds.isEmpty()) {
            LOGGER.info("No active ads available for userId={}, songId={}", userId, songId);
            return null;
        }

        Ad selectedAd = activeAds.get(ThreadLocalRandom.current().nextInt(activeAds.size()));
        AdImpression impression = new AdImpression();
        impression.setAdId(selectedAd.getId());
        impression.setUserId(userId);
        impression.setSongId(songId);
        impression.setPlayedAt(now);
        adImpressionRepository.save(impression);

        LOGGER.info("Ad selected for playback: adId={}, userId={}, songId={}", selectedAd.getId(), userId, songId);
        return selectedAd;
    }
}

