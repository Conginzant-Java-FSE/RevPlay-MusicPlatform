package com.revplay.revplay.service.impl;

import com.revplay.revplay.dto.request.AlbumCreateRequest;
import com.revplay.revplay.dto.response.AlbumResponse;
import com.revplay.revplay.dto.request.AlbumUpdateRequest;
import com.revplay.revplay.entity.Album;
import com.revplay.revplay.entity.Artist;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.exception.UnauthorizedException;
import com.revplay.revplay.mapper.AlbumMapper;
import com.revplay.revplay.repository.AlbumRepository;
import com.revplay.revplay.service.AlbumService;
import com.revplay.revplay.util.CurrentArtistResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlbumServiceImpl implements AlbumService {

    private static final Logger log = LoggerFactory.getLogger(AlbumServiceImpl.class);

    private final AlbumRepository albumRepository;
    private final CurrentArtistResolver currentArtistResolver;
    private final AlbumMapper albumMapper;

    public AlbumServiceImpl(
            AlbumRepository albumRepository,
            CurrentArtistResolver currentArtistResolver,
            AlbumMapper albumMapper) {
        this.albumRepository = albumRepository;
        this.currentArtistResolver = currentArtistResolver;
        this.albumMapper = albumMapper;
    }

    @Override
    @Transactional
    public AlbumResponse createAlbum(AlbumCreateRequest request) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        Album album = albumMapper.toEntity(request, artist);

        Album saved = albumRepository.save(album);
        log.info("Album created. albumId={}, artistId={}", saved.getAlbumId(), artist.getArtistId());

        return albumMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AlbumResponse updateAlbum(Long albumId, AlbumUpdateRequest request) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found: " + albumId));

        if (!album.getArtist().getArtistId().equals(artist.getArtistId())) {
            throw new UnauthorizedException("You are not allowed to update this album");
        }

        albumMapper.applyUpdate(request, album);

        Album saved = albumRepository.save(album);
        log.info("Album updated. albumId={}, artistId={}", saved.getAlbumId(), artist.getArtistId());

        return albumMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponse getAlbumById(Long albumId) {

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found: " + albumId));

        return albumMapper.toResponse(album);
    }
}

