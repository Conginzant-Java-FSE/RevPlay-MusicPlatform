package com.revplay.musicplatform.playlist.service.impl;

import com.revplay.musicplatform.audit.service.AuditLogService;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.exception.DuplicateResourceException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.playlist.dto.request.*;
import com.revplay.musicplatform.playlist.dto.response.PlaylistDetailResponse;
import com.revplay.musicplatform.playlist.dto.response.PlaylistFollowResponse;
import com.revplay.musicplatform.playlist.dto.response.PlaylistResponse;
import com.revplay.musicplatform.playlist.dto.response.PlaylistSongResponse;
import com.revplay.musicplatform.playlist.entity.Playlist;
import com.revplay.musicplatform.playlist.entity.PlaylistFollow;
import com.revplay.musicplatform.playlist.entity.PlaylistSong;
import com.revplay.musicplatform.playlist.mapper.PlaylistMapper;
import com.revplay.musicplatform.playlist.mapper.PlaylistSongMapper;
import com.revplay.musicplatform.playlist.repository.PlaylistFollowRepository;
import com.revplay.musicplatform.playlist.repository.PlaylistRepository;
import com.revplay.musicplatform.playlist.repository.PlaylistSongRepository;
import com.revplay.musicplatform.security.AuthContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplTest {

    private static final Long PLAYLIST_ID = 10L;
    private static final Long OWNER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;
    private static final Long SONG_ID = 300L;
    private static final String PLAYLIST_NAME = "Roadtrip";

    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private PlaylistSongRepository playlistSongRepository;
    @Mock
    private PlaylistFollowRepository playlistFollowRepository;
    @Mock
    private PlaylistMapper playlistMapper;
    @Mock
    private PlaylistSongMapper playlistSongMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AuthContextUtil authContextUtil;

    @InjectMocks
    private PlaylistServiceImpl service;

    private Playlist activePublicPlaylist;
    private Playlist activePrivatePlaylist;

    @BeforeEach
    void setUp() {
        activePublicPlaylist = Playlist.builder()
                .id(PLAYLIST_ID)
                .userId(OWNER_ID)
                .name(PLAYLIST_NAME)
                .isPublic(true)
                .isActive(true)
                .build();

        activePrivatePlaylist = Playlist.builder()
                .id(PLAYLIST_ID)
                .userId(OWNER_ID)
                .name(PLAYLIST_NAME)
                .isPublic(false)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("createPlaylist: happy path saves and audits")
    void createPlaylist_happyPath() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName(PLAYLIST_NAME);
        Playlist toSave = Playlist.builder().name(PLAYLIST_NAME).build();
        Playlist saved = Playlist.builder().id(PLAYLIST_ID).name(PLAYLIST_NAME).userId(OWNER_ID).build();
        PlaylistResponse response = PlaylistResponse.builder().id(PLAYLIST_ID).songCount(0).build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistMapper.toEntity(request, OWNER_ID)).thenReturn(toSave);
        when(playlistRepository.save(toSave)).thenReturn(saved);
        when(playlistMapper.toResponse(saved, 0, 0)).thenReturn(response);

        PlaylistResponse actual = service.createPlaylist(request);

        assertThat(actual.getSongCount()).isZero();
        verify(auditLogService).logInternal(any(), eq(OWNER_ID), any(), eq(PLAYLIST_ID), any());
    }

    @Test
    @DisplayName("getPlaylistById: public playlist with unauthenticated user returns details")
    void getPlaylistById_publicUnauthenticated_success() {
        PlaylistDetailResponse detail = PlaylistDetailResponse.builder().id(PLAYLIST_ID).build();
        PlaylistSong song = PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(SONG_ID).position(1).build();
        PlaylistSongResponse songResponse = PlaylistSongResponse.builder().songId(SONG_ID).position(1).build();

        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(authContextUtil.getCurrentUserIdOrNull()).thenReturn(null);
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(PLAYLIST_ID)).thenReturn(List.of(song));
        when(playlistFollowRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(1L);
        when(playlistMapper.toDetailResponse(activePublicPlaylist, 1L, 1L)).thenReturn(detail);
        when(playlistSongMapper.toResponse(song)).thenReturn(songResponse);

        PlaylistDetailResponse actual = service.getPlaylistById(PLAYLIST_ID);

        assertThat(actual.getSongs()).hasSize(1);
        verify(playlistRepository).findById(PLAYLIST_ID);
    }

    @Test
    @DisplayName("getPlaylistById: private playlist owner can read")
    void getPlaylistById_privateOwner_success() {
        PlaylistDetailResponse detail = PlaylistDetailResponse.builder().id(PLAYLIST_ID).songs(List.of()).build();
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePrivatePlaylist));
        when(authContextUtil.getCurrentUserIdOrNull()).thenReturn(OWNER_ID);
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(PLAYLIST_ID)).thenReturn(List.of());
        when(playlistFollowRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(0L);
        when(playlistMapper.toDetailResponse(activePrivatePlaylist, 0L, 0L)).thenReturn(detail);

        PlaylistDetailResponse actual = service.getPlaylistById(PLAYLIST_ID);

        assertThat(actual.getId()).isEqualTo(PLAYLIST_ID);
    }

    @Test
    @DisplayName("getPlaylistById: private playlist non-owner denied")
    void getPlaylistById_privateNonOwner_denied() {
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePrivatePlaylist));
        when(authContextUtil.getCurrentUserIdOrNull()).thenReturn(OTHER_USER_ID);

        assertThatThrownBy(() -> service.getPlaylistById(PLAYLIST_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("This playlist is private");
    }

    @Test
    @DisplayName("getPlaylistById: inactive playlist not found")
    void getPlaylistById_inactive_notFound() {
        Playlist inactive = Playlist.builder().id(PLAYLIST_ID).isActive(false).build();
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.getPlaylistById(PLAYLIST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updatePlaylist: happy path updates mutable fields")
    void updatePlaylist_happyPath() {
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setName("Updated");
        request.setDescription("Updated Description");
        request.setIsPublic(false);

        Playlist updated = Playlist.builder().id(PLAYLIST_ID).userId(OWNER_ID).name("Updated").isPublic(false).isActive(true).build();
        PlaylistResponse response = PlaylistResponse.builder().id(PLAYLIST_ID).name("Updated").build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistRepository.save(activePublicPlaylist)).thenReturn(updated);
        when(playlistSongRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(2L);
        when(playlistFollowRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(3L);
        when(playlistMapper.toResponse(updated, 2L, 3L)).thenReturn(response);

        PlaylistResponse actual = service.updatePlaylist(PLAYLIST_ID, request);

        assertThat(actual.getName()).isEqualTo("Updated");
        verify(playlistRepository).save(activePublicPlaylist);
    }

    @Test
    @DisplayName("updatePlaylist: all request fields null keeps existing values")
    void updatePlaylist_allNull_noOp() {
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        Playlist original = Playlist.builder().id(PLAYLIST_ID).userId(OWNER_ID).name(PLAYLIST_NAME).isPublic(true).isActive(true).build();
        PlaylistResponse response = PlaylistResponse.builder().id(PLAYLIST_ID).name(PLAYLIST_NAME).isPublic(true).build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(original));
        when(playlistRepository.save(original)).thenReturn(original);
        when(playlistSongRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(0L);
        when(playlistFollowRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(0L);
        when(playlistMapper.toResponse(original, 0L, 0L)).thenReturn(response);

        PlaylistResponse actual = service.updatePlaylist(PLAYLIST_ID, request);

        assertThat(actual.getName()).isEqualTo(PLAYLIST_NAME);
        assertThat(original.getIsPublic()).isTrue();
    }

    @Test
    @DisplayName("updatePlaylist: non-owner denied")
    void updatePlaylist_nonOwner_denied() {
        when(authContextUtil.requireCurrentUserId()).thenReturn(OTHER_USER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));

        assertThatThrownBy(() -> service.updatePlaylist(PLAYLIST_ID, new UpdatePlaylistRequest()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not own this playlist");
    }

    @Test
    @DisplayName("deletePlaylist: owner soft deletes and logs")
    void deletePlaylist_owned_softDelete() {
        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));

        service.deletePlaylist(PLAYLIST_ID);

        assertThat(activePublicPlaylist.getIsActive()).isFalse();
        verify(playlistRepository).save(activePublicPlaylist);
        verify(auditLogService).logInternal(any(), eq(OWNER_ID), any(), eq(PLAYLIST_ID), any());
    }

    @Test
    @DisplayName("getPublicPlaylists: maps pageable and content")
    void getPublicPlaylists_success() {
        Page<Playlist> page = new PageImpl<>(List.of(activePublicPlaylist));
        PlaylistResponse response = PlaylistResponse.builder().id(PLAYLIST_ID).build();

        when(playlistRepository.findByIsPublicTrueAndIsActiveTrue(any(Pageable.class))).thenReturn(page);
        when(playlistSongRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(2L);
        when(playlistFollowRepository.countByPlaylistId(PLAYLIST_ID)).thenReturn(1L);
        when(playlistMapper.toResponse(activePublicPlaylist, 2L, 1L)).thenReturn(response);

        PagedResponseDto<PlaylistResponse> actual = service.getPublicPlaylists(1, 5);

        assertThat(actual.getContent()).hasSize(1);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(playlistRepository).findByIsPublicTrueAndIsActiveTrue(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("getMyPlaylists: empty page returns empty content")
    void getMyPlaylists_empty() {
        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findByUserIdAndIsActiveTrue(eq(OWNER_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        PagedResponseDto<PlaylistResponse> actual = service.getMyPlaylists(0, 10);

        assertThat(actual.getContent()).isEmpty();
    }

    @Test
    @DisplayName("addSongToPlaylist: no position appends to end")
    void addSongToPlaylist_noPosition_nonEmpty_appends() {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);

        PlaylistSong saved = PlaylistSong.builder().id(1L).playlistId(PLAYLIST_ID).songId(SONG_ID).position(4).build();
        PlaylistSongResponse mapped = PlaylistSongResponse.builder().id(1L).position(4).songId(SONG_ID).build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(PLAYLIST_ID, SONG_ID)).thenReturn(false);
        when(playlistSongRepository.findMaxPositionByPlaylistId(PLAYLIST_ID)).thenReturn(3);
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenReturn(saved);
        when(playlistSongMapper.toResponse(saved)).thenReturn(mapped);

        PlaylistSongResponse actual = service.addSongToPlaylist(PLAYLIST_ID, request);

        assertThat(actual.getPosition()).isEqualTo(4);
        verify(auditLogService).logInternal(any(), eq(OWNER_ID), any(), eq(PLAYLIST_ID), any());
    }

    @Test
    @DisplayName("addSongToPlaylist: empty playlist defaults to position 1")
    void addSongToPlaylist_noPosition_empty_savesAtOne() {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);

        PlaylistSong saved = PlaylistSong.builder().id(1L).playlistId(PLAYLIST_ID).songId(SONG_ID).position(1).build();
        PlaylistSongResponse mapped = PlaylistSongResponse.builder().id(1L).position(1).songId(SONG_ID).build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(PLAYLIST_ID, SONG_ID)).thenReturn(false);
        when(playlistSongRepository.findMaxPositionByPlaylistId(PLAYLIST_ID)).thenReturn(0);
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenReturn(saved);
        when(playlistSongMapper.toResponse(saved)).thenReturn(mapped);

        PlaylistSongResponse actual = service.addSongToPlaylist(PLAYLIST_ID, request);

        assertThat(actual.getPosition()).isEqualTo(1);
        verify(playlistSongRepository, never()).findByPlaylistIdOrderByPositionAsc(PLAYLIST_ID);
    }

    @Test
    @DisplayName("addSongToPlaylist: insert in middle shifts existing songs")
    void addSongToPlaylist_positionTwo_shifts() {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);
        request.setPosition(2);

        PlaylistSong s1 = PlaylistSong.builder().id(11L).playlistId(PLAYLIST_ID).songId(1L).position(1).build();
        PlaylistSong s2 = PlaylistSong.builder().id(12L).playlistId(PLAYLIST_ID).songId(2L).position(2).build();
        PlaylistSong s3 = PlaylistSong.builder().id(13L).playlistId(PLAYLIST_ID).songId(3L).position(3).build();
        PlaylistSong inserted = PlaylistSong.builder().id(99L).playlistId(PLAYLIST_ID).songId(SONG_ID).position(2).build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(PLAYLIST_ID, SONG_ID)).thenReturn(false);
        when(playlistSongRepository.findMaxPositionByPlaylistId(PLAYLIST_ID)).thenReturn(3);
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(PLAYLIST_ID)).thenReturn(List.of(s1, s2, s3));
        when(playlistSongRepository.save(any(PlaylistSong.class))).thenReturn(inserted);
        when(playlistSongMapper.toResponse(inserted)).thenReturn(PlaylistSongResponse.builder().position(2).build());

        PlaylistSongResponse actual = service.addSongToPlaylist(PLAYLIST_ID, request);

        assertThat(actual.getPosition()).isEqualTo(2);
        assertThat(s2.getPosition()).isEqualTo(3);
        assertThat(s3.getPosition()).isEqualTo(4);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 3",
            "5, 3"
    })
    @DisplayName("addSongToPlaylist: invalid position throws IllegalArgumentException")
    void addSongToPlaylist_invalidPosition_throws(int requestedPosition, int maxPosition) {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);
        request.setPosition(requestedPosition);

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(PLAYLIST_ID, SONG_ID)).thenReturn(false);
        when(playlistSongRepository.findMaxPositionByPlaylistId(PLAYLIST_ID)).thenReturn(maxPosition);

        assertThatThrownBy(() -> service.addSongToPlaylist(PLAYLIST_ID, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("addSongToPlaylist: duplicate song throws DuplicateResourceException")
    void addSongToPlaylist_duplicate_throws() {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.existsByPlaylistIdAndSongId(PLAYLIST_ID, SONG_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.addSongToPlaylist(PLAYLIST_ID, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("removeSongFromPlaylist: removes and rebalances")
    void removeSongFromPlaylist_happyPath_rebalances() {
        PlaylistSong existing = PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(SONG_ID).position(2).build();
        PlaylistSong after = PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(301L).position(3).build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.findByPlaylistIdAndSongId(PLAYLIST_ID, SONG_ID)).thenReturn(Optional.of(existing));
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(PLAYLIST_ID)).thenReturn(List.of(after));

        service.removeSongFromPlaylist(PLAYLIST_ID, SONG_ID);

        assertThat(after.getPosition()).isEqualTo(2);
        verify(playlistSongRepository).deleteByPlaylistIdAndSongId(PLAYLIST_ID, SONG_ID);
    }

    @Test
    @DisplayName("reorderPlaylistSongs: valid full reorder updates all positions")
    void reorderPlaylistSongs_validFullReorder_success() {
        PlaylistSong current1 = PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(1L).position(1).build();
        PlaylistSong current2 = PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(2L).position(2).build();
        PlaylistSong current3 = PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(3L).position(3).build();

        SongPositionRequest p1 = new SongPositionRequest();
        p1.setSongId(1L);
        p1.setPosition(3);
        SongPositionRequest p2 = new SongPositionRequest();
        p2.setSongId(2L);
        p2.setPosition(1);
        SongPositionRequest p3 = new SongPositionRequest();
        p3.setSongId(3L);
        p3.setPosition(2);

        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongs(List.of(p1, p2, p3));

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(PLAYLIST_ID))
                .thenReturn(List.of(current1, current2, current3))
                .thenReturn(List.of(current2, current3, current1));
        when(playlistSongRepository.findByPlaylistIdAndSongId(PLAYLIST_ID, 1L)).thenReturn(Optional.of(current1));
        when(playlistSongRepository.findByPlaylistIdAndSongId(PLAYLIST_ID, 2L)).thenReturn(Optional.of(current2));
        when(playlistSongRepository.findByPlaylistIdAndSongId(PLAYLIST_ID, 3L)).thenReturn(Optional.of(current3));
        when(playlistSongMapper.toResponse(any(PlaylistSong.class)))
                .thenAnswer(invocation -> PlaylistSongResponse.builder()
                        .songId(((PlaylistSong) invocation.getArgument(0)).getSongId())
                        .position(((PlaylistSong) invocation.getArgument(0)).getPosition())
                        .build());

        List<PlaylistSongResponse> actual = service.reorderPlaylistSongs(PLAYLIST_ID, request);

        assertThat(actual).hasSize(3);
        assertThat(current1.getPosition()).isEqualTo(3);
        assertThat(current2.getPosition()).isEqualTo(1);
        assertThat(current3.getPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("reorderPlaylistSongs: wrong count throws IllegalArgumentException")
    void reorderPlaylistSongs_wrongCount_throws() {
        SongPositionRequest p1 = new SongPositionRequest();
        p1.setSongId(1L);
        p1.setPosition(1);

        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongs(List.of(p1));

        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistSongRepository.findByPlaylistIdOrderByPositionAsc(PLAYLIST_ID))
                .thenReturn(List.of(
                        PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(1L).position(1).build(),
                        PlaylistSong.builder().playlistId(PLAYLIST_ID).songId(2L).position(2).build()
                ));

        assertThatThrownBy(() -> service.reorderPlaylistSongs(PLAYLIST_ID, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("followPlaylist: happy path saves and returns")
    void followPlaylist_happyPath() {
        PlaylistFollow saved = PlaylistFollow.builder()
                .id(1L)
                .playlistId(PLAYLIST_ID)
                .followerUserId(OTHER_USER_ID)
                .followedAt(LocalDateTime.now())
                .build();

        when(authContextUtil.requireCurrentUserId()).thenReturn(OTHER_USER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistFollowRepository.existsByPlaylistIdAndFollowerUserId(PLAYLIST_ID, OTHER_USER_ID)).thenReturn(false);
        when(playlistFollowRepository.save(any(PlaylistFollow.class))).thenReturn(saved);

        PlaylistFollowResponse actual = service.followPlaylist(PLAYLIST_ID);

        assertThat(actual.getFollowerUserId()).isEqualTo(OTHER_USER_ID);
        verify(auditLogService).logInternal(any(), eq(OTHER_USER_ID), any(), eq(PLAYLIST_ID), any());
    }

    @Test
    @DisplayName("followPlaylist: own playlist throws DuplicateResourceException")
    void followPlaylist_ownPlaylist_throws() {
        when(authContextUtil.requireCurrentUserId()).thenReturn(OWNER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));

        assertThatThrownBy(() -> service.followPlaylist(PLAYLIST_ID))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("You cannot follow your own playlist");
    }

    @Test
    @DisplayName("followPlaylist: private non-owner denied")
    void followPlaylist_privateNonOwner_denied() {
        when(authContextUtil.requireCurrentUserId()).thenReturn(OTHER_USER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePrivatePlaylist));

        assertThatThrownBy(() -> service.followPlaylist(PLAYLIST_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Cannot follow a private playlist");
    }

    @Test
    @DisplayName("unfollowPlaylist: removes existing follow")
    void unfollowPlaylist_happyPath() {
        PlaylistFollow follow = PlaylistFollow.builder().id(1L).playlistId(PLAYLIST_ID).followerUserId(OTHER_USER_ID).build();
        when(authContextUtil.requireCurrentUserId()).thenReturn(OTHER_USER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistFollowRepository.findByPlaylistIdAndFollowerUserId(PLAYLIST_ID, OTHER_USER_ID)).thenReturn(Optional.of(follow));

        service.unfollowPlaylist(PLAYLIST_ID);

        verify(playlistFollowRepository).delete(follow);
        verify(auditLogService).logInternal(any(), eq(OTHER_USER_ID), any(), eq(PLAYLIST_ID), any());
    }

    @Test
    @DisplayName("unfollowPlaylist: not following throws ResourceNotFoundException")
    void unfollowPlaylist_notFollowing_throws() {
        when(authContextUtil.requireCurrentUserId()).thenReturn(OTHER_USER_ID);
        when(playlistRepository.findById(PLAYLIST_ID)).thenReturn(Optional.of(activePublicPlaylist));
        when(playlistFollowRepository.findByPlaylistIdAndFollowerUserId(PLAYLIST_ID, OTHER_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.unfollowPlaylist(PLAYLIST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
