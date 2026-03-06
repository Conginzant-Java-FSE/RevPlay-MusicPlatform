package com.revplay.musicplatform.catalog.service.impl;

import com.revplay.musicplatform.artist.entity.Artist;
import com.revplay.musicplatform.artist.enums.ArtistType;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.audit.event.SongDeletedEvent;
import com.revplay.musicplatform.catalog.dto.request.SongCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.SongUpdateRequest;
import com.revplay.musicplatform.catalog.dto.request.SongVisibilityRequest;
import com.revplay.musicplatform.catalog.dto.response.SongResponse;
import com.revplay.musicplatform.catalog.entity.Song;
import com.revplay.musicplatform.catalog.enums.ContentVisibility;
import com.revplay.musicplatform.catalog.mapper.SongMapper;
import com.revplay.musicplatform.catalog.repository.SongRepository;
import com.revplay.musicplatform.catalog.service.ContentValidationService;
import com.revplay.musicplatform.catalog.util.AccessValidator;
import com.revplay.musicplatform.catalog.util.AudioMetadataService;
import com.revplay.musicplatform.catalog.util.FileStorageService;
import com.revplay.musicplatform.catalog.util.SecurityUtil;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.exception.UnauthorizedException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SongServiceImplTest {

    @Mock
    private SongRepository songRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private SongMapper mapper;
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
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SongServiceImpl songService;

    private static final Long USER_ID = 1L;
    private static final Long ARTIST_ID = 10L;
    private static final Long SONG_ID = 100L;
    private static final Long ALBUM_ID = 50L;

    private Artist artist;
    private Song song;

    @BeforeEach
    void setUp() {
        artist = new Artist();
        artist.setArtistId(ARTIST_ID);
        artist.setUserId(USER_ID);
        artist.setArtistType(ArtistType.MUSIC);

        song = new Song();
        song.setSongId(SONG_ID);
        song.setArtistId(ARTIST_ID);
        song.setAlbumId(ALBUM_ID);
        song.setTitle("Test Song");
        song.setDurationSeconds(180);
        song.setFileUrl("/api/v1/files/songs/test.mp3");
        song.setIsActive(true);
    }

    @Test
    @DisplayName("create: ARTIST, valid file, no album")
    void create_Artist_ValidFile_NoAlbum() {
        SongCreateRequest request = new SongCreateRequest();
        request.setTitle("New Song");
        request.setDurationSeconds(200);
        MultipartFile file = mock(MultipartFile.class);

        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));
        when(audioMetadataService.resolveDurationSeconds(file, 200)).thenReturn(200);
        when(fileStorageService.storeSong(file)).thenReturn("stored_file.mp3");

        Song songEntity = new Song();
        when(mapper.toEntity(eq(request), eq(ARTIST_ID), anyString())).thenReturn(songEntity);
        when(songRepository.save(any(Song.class))).thenReturn(songEntity);
        when(mapper.toResponse(any(Song.class))).thenReturn(new SongResponse());

        SongResponse response = songService.create(request, file);

        assertThat(response).isNotNull();
        verify(contentValidationService).validateSongDuration(200);
        verify(songRepository).save(songEntity);
    }

    @Test
    @DisplayName("create: LISTENER role")
    void create_ListenerRole_ThrowsUnauthorized() {
        when(securityUtil.getUserRole()).thenReturn(UserRole.LISTENER.name());
        doThrow(new UnauthorizedException("Access denied")).when(accessValidator).requireArtistOrAdmin(anyString());

        assertThatThrownBy(() -> songService.create(new SongCreateRequest(), mock(MultipartFile.class)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("create: artist type = PODCAST")
    void create_PodcastArtist_ThrowsException() {
        artist.setArtistType(ArtistType.PODCAST);
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> songService.create(new SongCreateRequest(), mock(MultipartFile.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artist not allowed to upload songs");
    }

    @Test
    @DisplayName("create: ADMIN creates for any artist profile")
    void create_Admin_Success() {
        SongCreateRequest request = new SongCreateRequest();
        request.setTitle("Admin Song");
        request.setDurationSeconds(210);
        MultipartFile file = mock(MultipartFile.class);

        when(securityUtil.getUserId()).thenReturn(999L);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ADMIN.name());
        when(artistRepository.findByUserId(999L)).thenReturn(Optional.of(artist));
        when(audioMetadataService.resolveDurationSeconds(file, 210)).thenReturn(210);
        when(fileStorageService.storeSong(file)).thenReturn("admin_song.mp3");

        Song songEntity = new Song();
        when(mapper.toEntity(eq(request), eq(ARTIST_ID), anyString())).thenReturn(songEntity);
        when(songRepository.save(songEntity)).thenReturn(songEntity);
        when(mapper.toResponse(songEntity)).thenReturn(new SongResponse());

        SongResponse response = songService.create(request, file);

        assertThat(response).isNotNull();
        verify(songRepository).save(songEntity);
    }

    @Test
    @DisplayName("update: owned song, valid data")
    void update_OwnedSong_Success() {
        SongUpdateRequest request = new SongUpdateRequest();
        request.setTitle("Updated Title");
        request.setDurationSeconds(190);

        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(songRepository.save(any(Song.class))).thenReturn(song);
        when(mapper.toResponse(any(Song.class))).thenReturn(new SongResponse());

        SongResponse response = songService.update(SONG_ID, request);

        assertThat(response).isNotNull();
        verify(mapper).updateEntity(song, request);
        verify(songRepository).save(song);
    }

    @Test
    @DisplayName("update: song not found")
    void update_NotFound_ThrowsException() {
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> songService.update(SONG_ID, new SongUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Song not found");
    }

    @Test
    @DisplayName("update: song owned by different artist (non-admin)")
    void update_NotOwned_ThrowsException() {
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(999L); // Different user
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());

        assertThatThrownBy(() -> songService.update(SONG_ID, new SongUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Artist not found");
    }

    @Test
    @DisplayName("get: found")
    void get_Found_ReturnsResponse() {
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(mapper.toResponse(song)).thenReturn(new SongResponse());

        SongResponse response = songService.get(SONG_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("get: not found")
    void get_NotFound_ThrowsException() {
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> songService.get(SONG_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Song not found");
    }

    @Test
    @DisplayName("delete: owned song")
    void delete_OwnedSong_SoftDeletes() {
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());

        songService.delete(SONG_ID);

        assertThat(song.getIsActive()).isFalse();
        verify(songRepository).save(song);
        verify(eventPublisher).publishEvent(any(SongDeletedEvent.class));
    }

    @Test
    @DisplayName("listByArtist: artist has songs")
    void listByArtist_HasSongs_ReturnsPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Song> page = new PageImpl<>(List.of(song));
        when(songRepository.findByArtistId(ARTIST_ID, pageable)).thenReturn(page);
        when(mapper.toResponse(any(Song.class))).thenReturn(new SongResponse());

        Page<SongResponse> result = songService.listByArtist(ARTIST_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("updateVisibility: owned song")
    void updateVisibility_OwnedSong_Success() {
        SongVisibilityRequest request = new SongVisibilityRequest();
        request.setIsActive(false);
        request.setVisibility(ContentVisibility.UNLISTED);

        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(songRepository.save(any(Song.class))).thenReturn(song);
        when(mapper.toResponse(any(Song.class))).thenReturn(new SongResponse());

        songService.updateVisibility(SONG_ID, request);

        assertThat(song.getIsActive()).isFalse();
        assertThat(song.getVisibility()).isEqualTo(ContentVisibility.UNLISTED);
        verify(songRepository).save(song);
    }

    @Test
    @DisplayName("replaceAudio: valid fileUrl extraction")
    void replaceAudio_ValidExtraction_DeletesOldFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(fileStorageService.storeSong(file)).thenReturn("new_file.mp3");
        when(songRepository.save(any(Song.class))).thenReturn(song);

        songService.replaceAudio(SONG_ID, file);

        verify(fileStorageService).deleteSongFile("test.mp3");
        assertThat(song.getFileUrl()).endsWith("new_file.mp3");
    }

    @Test
    @DisplayName("replaceAudio: fileUrl=null")
    void replaceAudio_NullUrl_DoesNotDelete() {
        song.setFileUrl(null);
        MultipartFile file = mock(MultipartFile.class);
        when(songRepository.findById(SONG_ID)).thenReturn(Optional.of(song));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(fileStorageService.storeSong(file)).thenReturn("new_file.mp3");
        when(songRepository.save(any(Song.class))).thenReturn(song);

        songService.replaceAudio(SONG_ID, file);

        verify(fileStorageService, never()).deleteSongFile(anyString());
    }
}

