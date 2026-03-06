package com.revplay.musicplatform.systemplaylist.service.impl;

import com.revplay.musicplatform.catalog.repository.SongRepository;
import com.revplay.musicplatform.exception.BadRequestException;
import com.revplay.musicplatform.exception.DuplicateResourceException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.systemplaylist.dto.response.SystemPlaylistResponse;
import com.revplay.musicplatform.systemplaylist.entity.SystemPlaylist;
import com.revplay.musicplatform.systemplaylist.entity.SystemPlaylistSong;
import com.revplay.musicplatform.systemplaylist.repository.SystemPlaylistRepository;
import com.revplay.musicplatform.systemplaylist.repository.SystemPlaylistSongRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SystemPlaylistServiceImplTest {

    private static final Long PLAYLIST_ID = 10L;
    private static final String PLAYLIST_SLUG = "telugu-mix";
    private static final String MISSING_SLUG = "missing";

    @Mock
    private SystemPlaylistRepository systemPlaylistRepository;
    @Mock
    private SystemPlaylistSongRepository systemPlaylistSongRepository;
    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private SystemPlaylistServiceImpl service;

    @Test
    @DisplayName("getAllActivePlaylists returns mapped responses")
    void getAllActivePlaylists_hasItems_returnsMappedList() {
        SystemPlaylist playlist = playlist(PLAYLIST_ID, PLAYLIST_SLUG, true);
        playlist.setName("Telugu Mix");
        playlist.setDescription("Top Telugu tracks mixed by RevPlay");
        when(systemPlaylistRepository.findByIsActiveTrueAndDeletedAtIsNull()).thenReturn(List.of(playlist));

        List<SystemPlaylistResponse> responses = service.getAllActivePlaylists();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSlug()).isEqualTo(PLAYLIST_SLUG);
        assertThat(responses.get(0).getName()).isEqualTo("Telugu Mix");
    }

    @Test
    @DisplayName("getAllActivePlaylists with none active returns empty list")
    void getAllActivePlaylists_none_returnsEmptyList() {
        when(systemPlaylistRepository.findByIsActiveTrueAndDeletedAtIsNull()).thenReturn(List.of());

        List<SystemPlaylistResponse> responses = service.getAllActivePlaylists();

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("getSongIdsBySlug for valid active slug returns ordered song IDs")
    void getSongIdsBySlug_validSlug_returnsOrderedSongIds() {
        SystemPlaylist playlist = playlist(PLAYLIST_ID, PLAYLIST_SLUG, true);
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(PLAYLIST_SLUG)).thenReturn(Optional.of(playlist));
        when(systemPlaylistSongRepository.findBySystemPlaylistIdAndDeletedAtIsNullOrderByPositionAsc(PLAYLIST_ID))
                .thenReturn(List.of(mapping(PLAYLIST_ID, 111L, 1), mapping(PLAYLIST_ID, 222L, 2)));

        List<Long> ids = service.getSongIdsBySlug(PLAYLIST_SLUG);

        assertThat(ids).containsExactly(111L, 222L);
    }

    @Test
    @DisplayName("getSongIdsBySlug for missing slug throws ResourceNotFoundException")
    void getSongIdsBySlug_missingSlug_throwsNotFound() {
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(MISSING_SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSongIdsBySlug(MISSING_SLUG))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("System playlist not found: " + MISSING_SLUG);
    }

    @Test
    @DisplayName("getSongIdsBySlug for inactive playlist throws ResourceNotFoundException")
    void getSongIdsBySlug_inactive_throwsNotFound() {
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(PLAYLIST_SLUG))
                .thenReturn(Optional.of(playlist(PLAYLIST_ID, PLAYLIST_SLUG, false)));

        assertThatThrownBy(() -> service.getSongIdsBySlug(PLAYLIST_SLUG))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("System playlist not found: " + PLAYLIST_SLUG);
    }

    @Test
    @DisplayName("addSongsBySlug with null list throws BadRequestException")
    void addSongsBySlug_nullSongIds_throwsBadRequest() {
        assertThatThrownBy(() -> service.addSongsBySlug(PLAYLIST_SLUG, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("songIds must not be empty");
    }

    @Test
    @DisplayName("addSongsBySlug with duplicate song IDs throws BadRequestException")
    void addSongsBySlug_duplicateInRequest_throwsBadRequest() {
        assertThatThrownBy(() -> service.addSongsBySlug(PLAYLIST_SLUG, List.of(1L, 1L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("songIds contains duplicates");
    }

    @Test
    @DisplayName("addSongsBySlug with missing slug throws ResourceNotFoundException")
    void addSongsBySlug_missingSlug_throwsNotFound() {
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(MISSING_SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addSongsBySlug(MISSING_SLUG, List.of(1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("System playlist not found: " + MISSING_SLUG);
    }

    @Test
    @DisplayName("addSongsBySlug with missing song throws ResourceNotFoundException")
    void addSongsBySlug_songMissing_throwsNotFound() {
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(PLAYLIST_SLUG))
                .thenReturn(Optional.of(playlist(PLAYLIST_ID, PLAYLIST_SLUG, true)));
        when(systemPlaylistSongRepository.findBySystemPlaylistIdAndDeletedAtIsNullOrderByPositionAsc(PLAYLIST_ID))
                .thenReturn(List.of());
        when(songRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.addSongsBySlug(PLAYLIST_SLUG, List.of(999L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Song not found: 999");
    }

    @Test
    @DisplayName("addSongsBySlug when song already exists throws DuplicateResourceException")
    void addSongsBySlug_songAlreadyInPlaylist_throwsDuplicate() {
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(PLAYLIST_SLUG))
                .thenReturn(Optional.of(playlist(PLAYLIST_ID, PLAYLIST_SLUG, true)));
        when(systemPlaylistSongRepository.findBySystemPlaylistIdAndDeletedAtIsNullOrderByPositionAsc(PLAYLIST_ID))
                .thenReturn(List.of());
        when(songRepository.existsById(100L)).thenReturn(true);
        when(systemPlaylistSongRepository.existsBySystemPlaylistIdAndSongIdAndDeletedAtIsNull(PLAYLIST_ID, 100L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addSongsBySlug(PLAYLIST_SLUG, List.of(100L)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Song already exists in system playlist: 100");
    }

    @Test
    @DisplayName("addSongsBySlug with existing songs appends using sequential positions")
    void addSongsBySlug_existingMappings_appendsSequentialPositions() {
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(PLAYLIST_SLUG))
                .thenReturn(Optional.of(playlist(PLAYLIST_ID, PLAYLIST_SLUG, true)));
        when(systemPlaylistSongRepository.findBySystemPlaylistIdAndDeletedAtIsNullOrderByPositionAsc(PLAYLIST_ID))
                .thenReturn(List.of(mapping(PLAYLIST_ID, 10L, 1), mapping(PLAYLIST_ID, 20L, 2)));
        when(songRepository.existsById(30L)).thenReturn(true);
        when(songRepository.existsById(40L)).thenReturn(true);
        when(systemPlaylistSongRepository.existsBySystemPlaylistIdAndSongIdAndDeletedAtIsNull(PLAYLIST_ID, 30L))
                .thenReturn(false);
        when(systemPlaylistSongRepository.existsBySystemPlaylistIdAndSongIdAndDeletedAtIsNull(PLAYLIST_ID, 40L))
                .thenReturn(false);

        service.addSongsBySlug(PLAYLIST_SLUG, List.of(30L, 40L));

        ArgumentCaptor<SystemPlaylistSong> captor = ArgumentCaptor.forClass(SystemPlaylistSong.class);
        verify(systemPlaylistSongRepository, times(2)).save(captor.capture());

        List<SystemPlaylistSong> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getPosition()).isEqualTo(3);
        assertThat(saved.get(1).getPosition()).isEqualTo(4);
    }

    @Test
    @DisplayName("softDeletePlaylist sets inactive and deletedAt")
    void softDeletePlaylist_found_setsInactiveAndDeletedAt() {
        SystemPlaylist playlist = playlist(PLAYLIST_ID, PLAYLIST_SLUG, true);
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(PLAYLIST_SLUG)).thenReturn(Optional.of(playlist));

        service.softDeletePlaylist(PLAYLIST_SLUG);

        assertThat(playlist.getIsActive()).isFalse();
        assertThat(playlist.getDeletedAt()).isNotNull();
        verify(systemPlaylistRepository).save(playlist);
    }

    @Test
    @DisplayName("softDeletePlaylist for unknown slug throws ResourceNotFoundException")
    void softDeletePlaylist_missingSlug_throwsNotFound() {
        when(systemPlaylistRepository.findBySlugAndDeletedAtIsNull(MISSING_SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDeletePlaylist(MISSING_SLUG))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("System playlist not found: " + MISSING_SLUG);
        verify(systemPlaylistRepository, never()).save(any());
    }

    private SystemPlaylist playlist(Long id, String slug, boolean isActive) {
        SystemPlaylist playlist = new SystemPlaylist();
        playlist.setId(id);
        playlist.setSlug(slug);
        playlist.setName("Mix");
        playlist.setDescription("desc");
        playlist.setIsActive(isActive);
        return playlist;
    }

    private SystemPlaylistSong mapping(Long playlistId, Long songId, Integer position) {
        SystemPlaylistSong mapping = new SystemPlaylistSong();
        SystemPlaylist playlist = new SystemPlaylist();
        playlist.setId(playlistId);
        mapping.setSystemPlaylist(playlist);
        mapping.setSongId(songId);
        mapping.setPosition(position);
        return mapping;
    }
}
