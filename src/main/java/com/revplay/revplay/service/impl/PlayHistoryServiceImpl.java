package com.revplay.revplay.service.impl;

import com.revplay.revplay.dto.request.TrackPlayRequest;
import com.revplay.revplay.dto.response.PlayHistoryResponse;
import com.revplay.revplay.dto.response.RecentPlayResponse;
import com.revplay.revplay.entity.PlayHistory;
import com.revplay.revplay.mapper.PlayHistoryMapper;
import com.revplay.revplay.repository.PlayHistoryRepository;
import com.revplay.revplay.service.PlayHistoryService;
import com.revplay.revplay.util.PlaybackConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayHistoryServiceImpl implements PlayHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(PlayHistoryServiceImpl.class);

    private final PlayHistoryRepository historyRepository;
    private final PlayHistoryMapper historyMapper;

    @Transactional
    public PlayHistoryResponse trackPlay(Long userId, TrackPlayRequest request) {
        request.validate();

        PlayHistory history = new PlayHistory();
        history.setUserId(userId);        history.setSongId(request.getSongId());
        history.setEpisodeId(request.getEpisodeId());
        history.setPlayedAt(LocalDateTime.now());
        history.setCompleted(request.getCompleted());

        PlayHistory saved = historyRepository.save(history);

        logger.info("Tracked play: userId={}, contentId={}, completed={}",
                userId, getContentId(saved), request.getCompleted());

        return historyMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PlayHistoryResponse> getUserHistory(Long userId, Pageable pageable) {
        Page<PlayHistory> historyPage = historyRepository.findByUserIdOrderByPlayedAtDesc(userId, pageable);
        logger.debug("Retrieved history for userId={}: page {}, size {}",
                userId, pageable.getPageNumber(), historyPage.getNumberOfElements());
        return historyMapper.toResponsePage(historyPage);
    }

    @Transactional(readOnly = true)
    public List<RecentPlayResponse> getRecentPlays(Long userId) {
        Pageable limit = PageRequest.of(0, PlaybackConstants.RECENT_PLAYS_LIMIT);
        List<PlayHistory> recentPlays = historyRepository.findRecentByUserId(userId, limit);

        logger.debug("Retrieved recent plays for userId={}: {} items", userId, recentPlays.size());

        return historyMapper.toRecentPlayResponseList(recentPlays);
    }

    private Long getContentId(PlayHistory history) {
        return history.getSongId() != null ? history.getSongId() : history.getEpisodeId();
    }

    @Transactional
    public void clearHistory(Long userId) {
        logger.info("Clearing all play history for userId={}", userId);
        long deleted = historyRepository.deleteByUserId(userId);
        logger.info("Deleted {} play history records for userId={}", deleted, userId);
    }

}

