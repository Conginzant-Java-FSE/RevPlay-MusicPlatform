package com.revplay.musicplatform.catalog.service.impl;

import com.revplay.musicplatform.artist.entity.Artist;
import com.revplay.musicplatform.artist.enums.ArtistType;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.audit.event.PodcastDeletedEvent;
import com.revplay.musicplatform.catalog.dto.request.PodcastCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastUpdateRequest;
import com.revplay.musicplatform.catalog.dto.response.PodcastResponse;
import com.revplay.musicplatform.catalog.entity.Podcast;
import com.revplay.musicplatform.catalog.mapper.PodcastMapper;
import com.revplay.musicplatform.catalog.repository.PodcastRepository;
import com.revplay.musicplatform.catalog.util.AccessValidator;
import com.revplay.musicplatform.catalog.util.SecurityUtil;
import com.revplay.musicplatform.exception.BadRequestException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PodcastServiceImplTest {

    @Mock
    private PodcastRepository podcastRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private PodcastMapper mapper;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private AccessValidator accessValidator;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PodcastServiceImpl podcastService;

    private static final Long USER_ID = 1L;
    private static final Long ARTIST_ID = 10L;
    private static final Long PODCAST_ID = 100L;

    private Artist artist;
    private Podcast podcast;

    @BeforeEach
    void setUp() {
        artist = new Artist();
        artist.setArtistId(ARTIST_ID);
        artist.setUserId(USER_ID);
        artist.setArtistType(ArtistType.PODCAST);

        podcast = new Podcast();
        podcast.setPodcastId(PODCAST_ID);
        podcast.setArtistId(ARTIST_ID);
        podcast.setTitle("Test Podcast");
        podcast.setIsActive(true);
    }

    @Test
    @DisplayName("create: PODCAST artist succeeds")
    void create_PodcastArtist_Success() {
        PodcastCreateRequest request = new PodcastCreateRequest();
        request.setTitle("New Podcast");

        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));
        when(mapper.toEntity(eq(request), eq(ARTIST_ID))).thenReturn(podcast);
        when(podcastRepository.save(any(Podcast.class))).thenReturn(podcast);
        when(mapper.toResponse(any(Podcast.class))).thenReturn(new PodcastResponse());

        PodcastResponse response = podcastService.create(request);

        assertThat(response).isNotNull();
        verify(podcastRepository).save(podcast);
    }

    @Test
    @DisplayName("create: MUSIC artist fails")
    void create_MusicArtist_ThrowsBadRequest() {
        artist.setArtistType(ArtistType.MUSIC);
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> podcastService.create(new PodcastCreateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Artist not allowed to create podcasts");
    }

    @Test
    @DisplayName("get: podcast found")
    void get_Found_ReturnsResponse() {
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));
        when(mapper.toResponse(podcast)).thenReturn(new PodcastResponse());

        PodcastResponse response = podcastService.get(PODCAST_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("get: podcast is inactive")
    void get_Inactive_ThrowsNotFound() {
        podcast.setIsActive(false);
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));

        assertThatThrownBy(() -> podcastService.get(PODCAST_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Podcast not found");
    }

    @Test
    @DisplayName("update: owned podcast")
    void update_Owned_Success() {
        PodcastUpdateRequest request = new PodcastUpdateRequest();
        request.setTitle("Updated Title");

        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(podcastRepository.save(any(Podcast.class))).thenReturn(podcast);
        when(mapper.toResponse(any(Podcast.class))).thenReturn(new PodcastResponse());

        PodcastResponse response = podcastService.update(PODCAST_ID, request);

        assertThat(response).isNotNull();
        verify(mapper).updateEntity(podcast, request);
    }

    @Test
    @DisplayName("delete: owned podcast")
    void delete_Owned_Success() {
        when(podcastRepository.findById(PODCAST_ID)).thenReturn(Optional.of(podcast));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());

        podcastService.delete(PODCAST_ID);

        assertThat(podcast.getIsActive()).isFalse();
        verify(podcastRepository).save(podcast);
        verify(eventPublisher).publishEvent(any(PodcastDeletedEvent.class));
    }

    @Test
    @DisplayName("listRecommended: returns paginated list")
    void listRecommended_ReturnsPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Podcast> page = new PageImpl<>(List.of(podcast));
        when(podcastRepository.findRecommended(pageable)).thenReturn(page);
        when(mapper.toResponse(any(Podcast.class))).thenReturn(new PodcastResponse());

        Page<PodcastResponse> result = podcastService.listRecommended(pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
