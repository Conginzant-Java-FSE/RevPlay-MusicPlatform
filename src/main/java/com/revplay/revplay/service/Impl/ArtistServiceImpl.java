
package com.revplay.revplay.service.Impl;

import com.revplay.revplay.dto.request.ArtistCreateRequest;
import com.revplay.revplay.dto.response.ArtistResponse;
import com.revplay.revplay.dto.request.ArtistUpdateRequest;
import com.revplay.revplay.entity.Artist;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.exception.ConflictException;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.exception.UnauthorizedException;
import com.revplay.revplay.mapper.ArtistMapper;
import com.revplay.revplay.repository.ArtistRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.service.ArtistService;
import com.revplay.revplay.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtistServiceImpl implements ArtistService {

    private static final Logger log = LoggerFactory.getLogger(ArtistServiceImpl.class);

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository; // role-1 table, READ-ONLY usage
    private final ArtistMapper artistMapper;

    public ArtistServiceImpl(
            ArtistRepository artistRepository,
            UserRepository userRepository,
            ArtistMapper artistMapper
    ) {
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.artistMapper = artistMapper;
    }

    @Override
    @Transactional
    public ArtistResponse createArtistProfile(ArtistCreateRequest request) {

        User user = getCurrentUserOrThrow();

        // one artist profile per user (user_id UNIQUE)
        if (artistRepository.existsByUserId(user.getUserId())) {
            throw new ConflictException("Artist profile already exists for this user");
        }

        Artist artist = artistMapper.toEntity(request, user.getUserId());
        Artist saved = artistRepository.save(artist);

        log.info("Artist profile created. artistId={}, userId={}", saved.getArtistId(), user.getUserId());

        return artistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ArtistResponse updateMyArtistProfile(ArtistUpdateRequest request) {

        User user = getCurrentUserOrThrow();

        Artist artist = artistRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist profile not found for current user"));

        // update only allowed fields
        artistMapper.updateEntity(request, artist);

        Artist saved = artistRepository.save(artist);

        log.info("Artist profile updated. artistId={}, userId={}", saved.getArtistId(), user.getUserId());

        return artistMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistResponse getArtistPublic(Long artistId) {

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));

        return artistMapper.toResponse(artist);
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistResponse getMyArtistProfile() {

        User user = getCurrentUserOrThrow();

        Artist artist = artistRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist profile not found for current user"));

        return artistMapper.toResponse(artist);
    }

    // -------------------------
    // Internal helper
    // -------------------------
    private User getCurrentUserOrThrow() {
        String email = SecurityUtil.getCurrentUserEmail();
        if (email == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    }
}

