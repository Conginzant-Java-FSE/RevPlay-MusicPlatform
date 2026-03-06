package com.revplay.musicplatform.catalog.service.impl;

import com.revplay.musicplatform.artist.entity.Artist;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.audit.event.PodcastEpisodeDeletedEvent;
import com.revplay.musicplatform.catalog.dto.request.PodcastEpisodeCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastEpisodeUpdateRequest;
import com.revplay.musicplatform.catalog.dto.response.PodcastEpisodeResponse;
import com.revplay.musicplatform.catalog.entity.Podcast;
import com.revplay.musicplatform.catalog.entity.PodcastEpisode;
import com.revplay.musicplatform.catalog.mapper.PodcastEpisodeMapper;
import com.revplay.musicplatform.catalog.repository.PodcastEpisodeRepository;
import com.revplay.musicplatform.catalog.repository.PodcastRepository;
import com.revplay.musicplatform.catalog.service.ContentValidationService;
import com.revplay.musicplatform.catalog.util.AccessValidator;
import com.revplay.musicplatform.catalog.util.AudioMetadataService;
import com.revplay.musicplatform.catalog.util.FileStorageService;
import com.revplay.musicplatform.catalog.util.SecurityUtil;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PodcastEpisodeServiceImplTest {

    @Mock
    private PodcastEpisodeRepository episodeRepository;
    @Mock
    private PodcastRepository podcastRepository;
    @Mock
    private PodcastEpisodeMapper mapper;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private AccessValidator accessValidator;
    @Mock
    private ContentValidationService contentValidationService;
    @Mock
    private AudioMetadataService audioMetadataService;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PodcastEpisodeServiceImpl episodeService;

    private static final Long USER_ID = 1L;
    private static final Long ARTIST_ID = 10L;
    private static final Long PODCAST_ID = 100L;
    private static final Long EPISODE_ID = 1000L;

    private Artist artist;
    private Podcast podcast;
    private PodcastEpisode episode;

    @BeforeEach
    void setUp() {
        artist = new Artist();
        artist.setArtistId(ARTIST_ID);
        artist.setUserId(USER_ID);

        podcast = new Podcast();
        podcast.setPodcastId(PODCAST_ID);
        podcast.setArtistId(ARTIST_ID);
        podcast.setIsActive(true);

        episode = new PodcastEpisode();
        episode.setEpisodeId(EPISODE_ID);
        episode.setPodcastId(PODCAST_ID);
        episode.setTitle("Test Episode");
        episode.setAudioUrl("/api/v1/files/podcasts/test.mp3");
        episode.setDurationSeconds(300);
    }

    @Test
    @DisplayName("create: success")
    void create_Success() {
        PodcastEpisodeCreateRequest request = new PodcastEpisodeCreateRequest();
        request.setTitle("New Episode");
        request.setDurationSeconds(600);
        MultipartFile file = mock(MultipartFile.class);

        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(audioMetadataService.resolveDurationSeconds(file, 600)).thenReturn(600);
        when(fileStorageService.storePodcast(file)).thenReturn("stored_episode.mp3");

        when(mapper.toEntity(eq(request), eq(PODCAST_ID), anyString())).thenReturn(episode);
        when(episodeRepository.save(any(PodcastEpisode.class))).thenReturn(episode);
        when(mapper.toResponse(any(PodcastEpisode.class))).thenReturn(new PodcastEpisodeResponse());

        PodcastEpisodeResponse response = episodeService.create(PODCAST_ID, request, file);

        assertThat(response).isNotNull();
        verify(contentValidationService).validatePodcastEpisodeDuration(600);
        verify(episodeRepository).save(episode);
    }

    @Test
    @DisplayName("create: podcast not found")
    void create_PodcastNotFound_ThrowsException() {
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> episodeService.create(PODCAST_ID, new PodcastEpisodeCreateRequest(), mock(MultipartFile.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Podcast not found");
    }

    @Test
    @DisplayName("update: success")
    void update_Success() {
        PodcastEpisodeUpdateRequest request = new PodcastEpisodeUpdateRequest();
        request.setTitle("Updated Title");
        request.setDurationSeconds(400);

        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());

        when(episodeRepository.save(any(PodcastEpisode.class))).thenReturn(episode);
        when(mapper.toResponse(any(PodcastEpisode.class))).thenReturn(new PodcastEpisodeResponse());

        PodcastEpisodeResponse response = episodeService.update(PODCAST_ID, EPISODE_ID, request);

        assertThat(response).isNotNull();
        verify(contentValidationService).validatePodcastEpisodeDuration(400);
        verify(mapper).updateEntity(episode, request);
    }

    @Test
    @DisplayName("get: success")
    void get_Success() {
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(mapper.toResponse(episode)).thenReturn(new PodcastEpisodeResponse());

        PodcastEpisodeResponse response = episodeService.get(PODCAST_ID, EPISODE_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("get: podcast ID mismatch")
    void get_PodcastMismatch_ThrowsException() {
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));

        assertThatThrownBy(() -> episodeService.get(999L, EPISODE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Episode not found");
    }

    @Test
    @DisplayName("delete: success")
    void delete_Success() {
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());

        episodeService.delete(PODCAST_ID, EPISODE_ID);

        verify(episodeRepository).delete(episode);
        verify(fileStorageService).deletePodcastFile("test.mp3");
        verify(eventPublisher).publishEvent(any(PodcastEpisodeDeletedEvent.class));
    }

    @Test
    @DisplayName("listByPodcast: returns paginated list")
    void listByPodcast_ReturnsPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PodcastEpisode> page = new PageImpl<>(List.of(episode));
        when(episodeRepository.findByPodcastId(PODCAST_ID, pageable)).thenReturn(page);
        when(mapper.toResponse(any(PodcastEpisode.class))).thenReturn(new PodcastEpisodeResponse());

        Page<PodcastEpisodeResponse> result = episodeService.listByPodcast(PODCAST_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("replaceAudio: success deletes old file")
    void replaceAudio_Success() {
        MultipartFile file = mock(MultipartFile.class);
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());

        when(audioMetadataService.resolveDurationSeconds(file, 300)).thenReturn(350);
        when(fileStorageService.storePodcast(file)).thenReturn("new_episode.mp3");
        when(episodeRepository.save(any(PodcastEpisode.class))).thenReturn(episode);
        when(mapper.toResponse(any(PodcastEpisode.class))).thenReturn(new PodcastEpisodeResponse());

        episodeService.replaceAudio(PODCAST_ID, EPISODE_ID, file);

        verify(fileStorageService).deletePodcastFile("test.mp3");
        assertThat(episode.getAudioUrl()).endsWith("new_episode.mp3");
        assertThat(episode.getDurationSeconds()).isEqualTo(350);
    }
}
