package com.revplay.revplay.service.Impl;
import com.revplay.revplay.dto.request.ArtistSocialLinkCreate;
import com.revplay.revplay.dto.response.ArtistSocialLinkResponse;
import com.revplay.revplay.entity.Artist;
import com.revplay.revplay.entity.ArtistSocialLink;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.enums.SocialPlatform;
import com.revplay.revplay.exception.ConflictException;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.exception.UnauthorizedException;
import com.revplay.revplay.mapper.ArtistSocialLinkMapper;
import com.revplay.revplay.repository.ArtistRepository;
import com.revplay.revplay.repository.ArtistSocialLinkRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.service.ArtistSocialLinkService;
import com.revplay.revplay.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArtistSocialLinkServiceImpl implements ArtistSocialLinkService {

    private static final Logger log = LoggerFactory.getLogger(ArtistSocialLinkServiceImpl.class);

    private final ArtistSocialLinkRepository socialLinkRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final ArtistSocialLinkMapper socialLinkMapper;

    public ArtistSocialLinkServiceImpl(
            ArtistSocialLinkRepository socialLinkRepository,
            ArtistRepository artistRepository,
            UserRepository userRepository,
            ArtistSocialLinkMapper socialLinkMapper
    ) {
        this.socialLinkRepository = socialLinkRepository;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.socialLinkMapper = socialLinkMapper;
    }

    @Override
    @Transactional
    public ArtistSocialLinkResponse addMySocialLink(ArtistSocialLinkCreate request) {

        Artist artist = getCurrentArtistOrThrow();

        SocialPlatform platform = request.getPlatform();
        if (socialLinkRepository.existsByArtist_ArtistIdAndPlatform(artist.getArtistId(), platform)) {
            throw new ConflictException("Social link already exists for platform: " + platform);
        }

        ArtistSocialLink link = new ArtistSocialLink();
        link.setArtist(artist);
        link.setPlatform(platform);
        link.setUrl(request.getUrl());

        ArtistSocialLink saved = socialLinkRepository.save(link);

        log.info("Artist social link added. linkId={}, artistId={}, platform={}",
                saved.getLinkId(), artist.getArtistId(), platform);

        return socialLinkMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtistSocialLinkResponse> getArtistSocialLinks(Long artistId) {

        artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));

        return socialLinkRepository.findAllByArtist_ArtistIdOrderByLinkIdAsc(artistId)
                .stream()
                .map(socialLinkMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMySocialLink(Long linkId) {

        Artist artist = getCurrentArtistOrThrow();

        ArtistSocialLink link = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Social link not found: " + linkId));

        if (!link.getArtist().getArtistId().equals(artist.getArtistId())) {
            throw new UnauthorizedException("You are not allowed to delete this social link");
        }

        socialLinkRepository.delete(link);

        log.info("Artist social link deleted. linkId={}, artistId={}", linkId, artist.getArtistId());
    }

    private Artist getCurrentArtistOrThrow() {
        String email = SecurityUtil.getCurrentUserEmail();
        if (email == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        return artistRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist profile not found for current user"));
    }
}
