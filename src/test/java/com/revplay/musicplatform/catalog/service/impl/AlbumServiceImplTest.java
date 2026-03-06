package com.revplay.musicplatform.catalog.service.impl;

import com.revplay.musicplatform.artist.entity.Artist;
import com.revplay.musicplatform.artist.repository.ArtistRepository;
import com.revplay.musicplatform.audit.event.AlbumDeletedEvent;
import com.revplay.musicplatform.catalog.dto.request.AlbumCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.AlbumUpdateRequest;
import com.revplay.musicplatform.catalog.dto.response.AlbumResponse;
import com.revplay.musicplatform.catalog.entity.Album;
import com.revplay.musicplatform.catalog.entity.Song;
import com.revplay.musicplatform.catalog.mapper.AlbumMapper;
import com.revplay.musicplatform.catalog.repository.AlbumRepository;
import com.revplay.musicplatform.catalog.repository.SongRepository;
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
class AlbumServiceImplTest {

    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private AlbumMapper mapper;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private AccessValidator accessValidator;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AlbumServiceImpl albumService;

    private static final Long USER_ID = 1L;
    private static final Long ARTIST_ID = 10L;
    private static final Long ALBUM_ID = 100L;

    private Artist artist;
    private Album album;

    @BeforeEach
    void setUp() {
        artist = new Artist();
        artist.setArtistId(ARTIST_ID);
        artist.setUserId(USER_ID);

        album = new Album();
        album.setAlbumId(ALBUM_ID);
        album.setArtistId(ARTIST_ID);
        album.setTitle("Test Album");
        album.setIsActive(true);
    }

    @Test
    @DisplayName("create: ARTIST creates album")
    void create_Artist_Success() {
        AlbumCreateRequest request = new AlbumCreateRequest();
        request.setTitle("New Album");

        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(artistRepository.findByUserId(USER_ID)).thenReturn(Optional.of(artist));
        when(mapper.toEntity(eq(request), eq(ARTIST_ID))).thenReturn(album);
        when(albumRepository.save(any(Album.class))).thenReturn(album);
        when(mapper.toResponse(any(Album.class))).thenReturn(new AlbumResponse());

        AlbumResponse response = albumService.create(request);

        assertThat(response).isNotNull();
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("get: album found")
    void get_Found_ReturnsResponse() {
        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
        when(mapper.toResponse(album)).thenReturn(new AlbumResponse());

        AlbumResponse response = albumService.get(ALBUM_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("get: non-existent album")
    void get_NotFound_ThrowsException() {
        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.get(ALBUM_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album not found");
    }

    @Test
    @DisplayName("get: inactive album")
    void get_Inactive_ThrowsException() {
        album.setIsActive(false);
        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));

        assertThatThrownBy(() -> albumService.get(ALBUM_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Album not found");
    }

    @Test
    @DisplayName("update: owned album")
    void update_Owned_Success() {
        AlbumUpdateRequest request = new AlbumUpdateRequest();
        request.setTitle("Updated Album");

        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(albumRepository.save(any(Album.class))).thenReturn(album);
        when(mapper.toResponse(any(Album.class))).thenReturn(new AlbumResponse());

        AlbumResponse response = albumService.update(ALBUM_ID, request);

        assertThat(response).isNotNull();
        verify(mapper).updateEntity(album, request);
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("delete: owned album with no songs")
    void delete_OwnedNoSongs_Success() {
        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(songRepository.countByAlbumId(ALBUM_ID)).thenReturn(0L);

        albumService.delete(ALBUM_ID);

        assertThat(album.getIsActive()).isFalse();
        verify(albumRepository).save(album);
        verify(eventPublisher).publishEvent(any(AlbumDeletedEvent.class));
    }

    @Test
    @DisplayName("delete: album with songs")
    void delete_WithSongs_ThrowsException() {
        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(songRepository.countByAlbumId(ALBUM_ID)).thenReturn(5L);

        assertThatThrownBy(() -> albumService.delete(ALBUM_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot delete album with songs");
    }

    @Test
    @DisplayName("listByArtist: returns paginated list")
    void listByArtist_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> page = new PageImpl<>(List.of(album));
        when(albumRepository.findByArtistIdAndIsActiveTrue(ARTIST_ID, pageable)).thenReturn(page);
        when(mapper.toResponse(any(Album.class))).thenReturn(new AlbumResponse());

        Page<AlbumResponse> result = albumService.listByArtist(ARTIST_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("addSongToAlbum: success")
    void addSongToAlbum_Success() {
        Song song = new Song();
        song.setSongId(500L);
        song.setArtistId(ARTIST_ID);

        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(songRepository.findById(500L)).thenReturn(Optional.of(song));

        albumService.addSongToAlbum(ALBUM_ID, 500L);

        assertThat(song.getAlbumId()).isEqualTo(ALBUM_ID);
        verify(songRepository).save(song);
    }

    @Test
    @DisplayName("addSongToAlbum: artist mismatch")
    void addSongToAlbum_ArtistMismatch_ThrowsException() {
        Song song = new Song();
        song.setSongId(500L);
        song.setArtistId(999L); // Different artist

        when(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album));
        when(artistRepository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(securityUtil.getUserId()).thenReturn(USER_ID);
        when(securityUtil.getUserRole()).thenReturn(UserRole.ARTIST.name());
        when(songRepository.findById(500L)).thenReturn(Optional.of(song));

        assertThatThrownBy(() -> albumService.addSongToAlbum(ALBUM_ID, 500L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Song artist mismatch");
    }
}
