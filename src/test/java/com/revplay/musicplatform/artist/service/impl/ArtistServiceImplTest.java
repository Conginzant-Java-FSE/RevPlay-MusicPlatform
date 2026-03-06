package com.revplay.musicplatform.artist.service.impl;

import com.revplay.musicplatform.artist.dto.request.ArtistCreateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistUpdateRequest;
import com.revplay.musicplatform.artist.dto.request.ArtistVerifyRequest;
import com.revplay.musicplatform.artist.dto.response.ArtistResponse;
import com.revplay.musicplatform.artist.dto.response.ArtistSummaryResponse;
import com.revplay.musicplatform.artist.entity.Artist;
import com.revplay.musicplatform.artist.mapper.ArtistMapper;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.catalog.repository.AlbumRepository;
import com.revplay.musicplatform.catalog.repository.PodcastRepository;
import com.revplay.musicplatform.catalog.repository.SongRepository;
import com.revplay.musicplatform.catalog.util.AccessValidator;
import com.revplay.musicplatform.catalog.util.SecurityUtil;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.exception.ConflictException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    private static final Long ARTIST_ID = 11L;
    private static final Long OWNER_USER_ID = 21L;
    private static final Long OTHER_USER_ID = 99L;

    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private ArtistMapper artistMapper;
    @Mock
    private SongRepository songRepository;
    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private PodcastRepository podcastRepository;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private AccessValidator accessValidator;

    @InjectMocks
    private ArtistServiceImpl service;

    @Test
    @DisplayName("createArtist ARTIST role and no existing profile saves artist")
    void createArtist_artistRole_saves() {
        ArtistCreateRequest request = new ArtistCreateRequest();
        Artist entity = artist(ARTIST_ID, OWNER_USER_ID);
        Artist saved = artist(ARTIST_ID, OWNER_USER_ID);
        ArtistResponse response = new ArtistResponse();
        response.setArtistId(ARTIST_ID);

        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(securityUtil.getUserId()).thenReturn(OWNER_USER_ID);
        when(artistRepository.findByUserId(OWNER_USER_ID)).thenReturn(Optional.empty());
        when(artistMapper.toEntity(request, OWNER_USER_ID)).thenReturn(entity);
        when(artistRepository.save(entity)).thenReturn(saved);
        when(artistMapper.toResponse(saved)).thenReturn(response);

        ArtistResponse actual = service.createArtist(request);

        assertThat(actual.getArtistId()).isEqualTo(ARTIST_ID);
        verify(artistRepository).save(entity);
    }

    @Test
    @DisplayName("createArtist listener role throws AccessDeniedException")
    void createArtist_listenerRole_throws() {
        ArtistCreateRequest request = new ArtistCreateRequest();
        when(securityUtil.getUserRole()).thenReturn(UserRole.LISTENER.name());
        doThrow(new AccessDeniedException("Artists or admins only"))
                .when(accessValidator).requireArtistOrAdmin(UserRole.LISTENER.name());

        assertThatThrownBy(() -> service.createArtist(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("createArtist existing profile throws ConflictException")
    void createArtist_existingProfile_throwsConflict() {
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(securityUtil.getUserId()).thenReturn(OWNER_USER_ID);
        when(artistRepository.findByUserId(OWNER_USER_ID)).thenReturn(Optional.of(artist(ARTIST_ID, OWNER_USER_ID)));

        assertThatThrownBy(() -> service.createArtist(new ArtistCreateRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Artist profile already exists");
    }

    @Test
    @DisplayName("getArtist found returns ArtistResponse")
    void getArtist_found_returnsResponse() {
        Artist artist = artist(ARTIST_ID, OWNER_USER_ID);
        ArtistResponse response = new ArtistResponse();
        response.setArtistId(ARTIST_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response);

        ArtistResponse actual = service.getArtist(ARTIST_ID);

        assertThat(actual.getArtistId()).isEqualTo(ARTIST_ID);
    }

    @Test
    @DisplayName("getArtist missing throws ResourceNotFoundException")
    void getArtist_missing_throws() {
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getArtist(ARTIST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateArtist owner updates and saves")
    void updateArtist_owner_saves() {
        Artist existing = artist(ARTIST_ID, OWNER_USER_ID);
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        ArtistResponse response = new ArtistResponse();
        response.setArtistId(ARTIST_ID);

        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(securityUtil.getUserId()).thenReturn(OWNER_USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(existing));
        when(artistRepository.save(existing)).thenReturn(existing);
        when(artistMapper.toResponse(existing)).thenReturn(response);

        ArtistResponse actual = service.updateArtist(ARTIST_ID, request);

        assertThat(actual.getArtistId()).isEqualTo(ARTIST_ID);
        verify(artistMapper).updateEntity(existing, request);
    }

    @Test
    @DisplayName("updateArtist non owner artist throws ResourceNotFoundException")
    void updateArtist_nonOwnerArtist_throws() {
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(securityUtil.getUserId()).thenReturn(OTHER_USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist(ARTIST_ID, OWNER_USER_ID)));

        assertThatThrownBy(() -> service.updateArtist(ARTIST_ID, new ArtistUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateArtist ADMIN updates any artist")
    void updateArtist_admin_updatesAny() {
        Artist existing = artist(ARTIST_ID, OWNER_USER_ID);
        ArtistResponse response = new ArtistResponse();
        response.setArtistId(ARTIST_ID);

        when(securityUtil.getUserRole()).thenReturn(UserRole.ADMIN.name());
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(existing));
        when(artistRepository.save(existing)).thenReturn(existing);
        when(artistMapper.toResponse(existing)).thenReturn(response);

        ArtistResponse actual = service.updateArtist(ARTIST_ID, new ArtistUpdateRequest());

        assertThat(actual.getArtistId()).isEqualTo(ARTIST_ID);
        verify(artistRepository).save(existing);
    }

    @Test
    @DisplayName("verifyArtist admin sets verified true")
    void verifyArtist_admin_setsVerified() {
        Artist artist = artist(ARTIST_ID, OWNER_USER_ID);
        ArtistVerifyRequest request = new ArtistVerifyRequest();
        request.setVerified(true);
        ArtistResponse response = new ArtistResponse();
        response.setArtistId(ARTIST_ID);

        when(securityUtil.getUserRole()).thenReturn(UserRole.ADMIN.name());
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(artistRepository.save(artist)).thenReturn(artist);
        when(artistMapper.toResponse(artist)).thenReturn(response);

        ArtistResponse actual = service.verifyArtist(ARTIST_ID, request);

        assertThat(actual.getArtistId()).isEqualTo(ARTIST_ID);
        assertThat(artist.getVerified()).isTrue();
    }

    @Test
    @DisplayName("verifyArtist artist role throws AccessDeniedException")
    void verifyArtist_artistRole_throws() {
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        doThrow(new AccessDeniedException("Admin only"))
                .when(accessValidator).requireAdmin(UserRole.ARTIST.name());

        ArtistVerifyRequest request = new ArtistVerifyRequest();
        request.setVerified(true);

        assertThatThrownBy(() -> service.verifyArtist(ARTIST_ID, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getSummary returns counts for songs albums podcasts")
    void getSummary_returnsCounts() {
        Artist artist = artist(ARTIST_ID, OWNER_USER_ID);
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(songRepository.countByArtistId(ARTIST_ID)).thenReturn(12L);
        when(albumRepository.countByArtistIdAndIsActiveTrue(ARTIST_ID)).thenReturn(3L);
        when(podcastRepository.countByArtistIdAndIsActiveTrue(ARTIST_ID)).thenReturn(2L);

        ArtistSummaryResponse summary = service.getSummary(ARTIST_ID);

        assertThat(summary.getSongCount()).isEqualTo(12L);
        assertThat(summary.getAlbumCount()).isEqualTo(3L);
        assertThat(summary.getPodcastCount()).isEqualTo(2L);
    }

    private Artist artist(Long artistId, Long userId) {
        Artist artist = new Artist();
        artist.setArtistId(artistId);
        artist.setUserId(userId);
        artist.setVerified(false);
        return artist;
    }
}
