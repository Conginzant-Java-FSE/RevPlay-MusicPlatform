package com.revplay.revplay.service.impl;



import com.revplay.revplay.dto.request.PodcastCreateRequest;
import com.revplay.revplay.dto.request.PodcastUpdateRequest;
import com.revplay.revplay.dto.response.PodcastResponse;
import com.revplay.revplay.entity.Artist;
import com.revplay.revplay.entity.Podcast;
import com.revplay.revplay.entity.PodcastCategory;
import com.revplay.revplay.exception.BadRequestException;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.repository.ArtistRepository;
import com.revplay.revplay.repository.PodcastCategoryRepository;
import com.revplay.revplay.repository.PodcastRepository;
import com.revplay.revplay.service.PodcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PodcastServiceImpl implements PodcastService {

    private final PodcastRepository podcastRepository;
    private final ArtistRepository artistRepository;
    private final PodcastCategoryRepository categoryRepository;

    @Override
    public PodcastResponse createPodcast(PodcastCreateRequest request) {

        validateRequest(request);

        Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));

        PodcastCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (podcastRepository.existsByArtist_ArtistIdAndTitleIgnoreCase(
                request.getArtistId(), request.getTitle())) {
            throw new BadRequestException("Podcast title already exists for this artist");
        }

        Podcast podcast = new Podcast();
        podcast.setArtist(artist);
        podcast.setCategory(category);
        podcast.setTitle(request.getTitle().trim());
        podcast.setDescription(request.getDescription());

        Podcast saved = podcastRepository.save(podcast);

        log.info("Podcast created: id={}, artistId={}", saved.getPodcastId(), artist.getArtistId());

        return mapToResponse(saved);
    }

    @Override
    public PodcastResponse updatePodcast(Long podcastId, PodcastUpdateRequest request) {

        if (podcastId == null) throw new BadRequestException("podcastId is required");

        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast not found"));

        PodcastCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        podcast.setTitle(request.getTitle().trim());
        podcast.setDescription(request.getDescription());
        podcast.setCategory(category);

        log.info("Podcast updated: id={}", podcastId);

        return mapToResponse(podcast);
    }

    @Override
    @Transactional(readOnly = true)
    public PodcastResponse getPodcastById(Long podcastId) {

        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast not found"));

        return mapToResponse(podcast);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PodcastResponse> getPodcastsByArtist(Long artistId) {

        if (artistId == null) throw new BadRequestException("artistId is required");

        return podcastRepository.findByArtist_ArtistId(artistId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deletePodcast(Long podcastId) {

        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Podcast not found"));

        podcastRepository.delete(podcast);

        log.info("Podcast deleted: id={}", podcastId);
    }

    private void validateRequest(PodcastCreateRequest request) {
        if (request == null) throw new BadRequestException("Request body is required");
    }

    private PodcastResponse mapToResponse(Podcast podcast) {
        PodcastResponse dto = new PodcastResponse();
        dto.setPodcastId(podcast.getPodcastId());
        dto.setArtistId(podcast.getArtist().getArtistId());
        dto.setCategoryId(podcast.getCategory().getCategoryId());
        dto.setTitle(podcast.getTitle());
        dto.setDescription(podcast.getDescription());
        dto.setCreatedAt(podcast.getCreatedAt());
        return dto;
    }
}
