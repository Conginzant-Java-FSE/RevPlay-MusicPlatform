package com.revplay.musicplatform.playlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.exception.DuplicateResourceException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.playlist.dto.request.*;
import com.revplay.musicplatform.playlist.dto.response.PlaylistDetailResponse;
import com.revplay.musicplatform.playlist.dto.response.PlaylistFollowResponse;
import com.revplay.musicplatform.playlist.dto.response.PlaylistResponse;
import com.revplay.musicplatform.playlist.dto.response.PlaylistSongResponse;
import com.revplay.musicplatform.playlist.service.PlaylistService;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PlaylistController.class)
@Import(SecurityConfig.class)
class PlaylistControllerTest {

    private static final String BASE = "/api/v1/playlists";
    private static final Long PLAYLIST_ID = 10L;
    private static final Long SONG_ID = 500L;
    private static final Long USER_ID = 100L;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private PlaylistService playlistService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    PlaylistControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST /api/v1/playlists authenticated returns 201")
    void create_authenticated_returns201() throws Exception {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("My Playlist");
        when(playlistService.createPlaylist(any(CreatePlaylistRequest.class)))
                .thenReturn(PlaylistResponse.builder().id(PLAYLIST_ID).name("My Playlist").build());

        mockMvc.perform(post(BASE)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(PLAYLIST_ID));
    }

    @Test
    @DisplayName("POST /api/v1/playlists without auth returns forbidden")
    void create_noAuth_forbidden() throws Exception {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("My Playlist");

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/playlists missing name returns 400")
    void create_missingName_badRequest() throws Exception {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setDescription("x");

        mockMvc.perform(post(BASE)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/playlists/{id} returns 200")
    void getPlaylist_success() throws Exception {
        when(playlistService.getPlaylistById(PLAYLIST_ID))
                .thenReturn(PlaylistDetailResponse.builder().id(PLAYLIST_ID).build());

        mockMvc.perform(get(BASE + "/{playlistId}", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(PLAYLIST_ID));
    }

    @Test
    @DisplayName("GET /api/v1/playlists/{id} not found returns 404")
    void getPlaylist_notFound_404() throws Exception {
        when(playlistService.getPlaylistById(PLAYLIST_ID)).thenThrow(new ResourceNotFoundException("Playlist", PLAYLIST_ID));

        mockMvc.perform(get(BASE + "/{playlistId}", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/playlists/{id} non-owner returns 403")
    void update_nonOwner_forbidden() throws Exception {
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setName("Updated");
        when(playlistService.updatePlaylist(eq(PLAYLIST_ID), any(UpdatePlaylistRequest.class)))
                .thenThrow(new AccessDeniedException("You do not own this playlist"));

        mockMvc.perform(put(BASE + "/{playlistId}", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/playlists/{id}/songs duplicate returns 409")
    void addSong_duplicate_conflict() throws Exception {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);
        when(playlistService.addSongToPlaylist(eq(PLAYLIST_ID), any(AddSongToPlaylistRequest.class)))
                .thenThrow(new DuplicateResourceException("duplicate"));

        mockMvc.perform(post(BASE + "/{playlistId}/songs", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/v1/playlists/{id}/songs/{songId} not in playlist returns 404")
    void removeSong_notFound_404() throws Exception {
        doThrow(new ResourceNotFoundException("Song", SONG_ID))
                .when(playlistService).removeSongFromPlaylist(PLAYLIST_ID, SONG_ID);

        mockMvc.perform(delete(BASE + "/{playlistId}/songs/{songId}", PLAYLIST_ID, SONG_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/playlists/{id}/songs/reorder invalid request returns 400")
    void reorder_invalidRequest_400() throws Exception {
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongs(List.of());

        mockMvc.perform(put(BASE + "/{playlistId}/songs/reorder", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/playlists/{id}/follow already following returns 409")
    void follow_alreadyFollowing_conflict() throws Exception {
        when(playlistService.followPlaylist(PLAYLIST_ID)).thenThrow(new DuplicateResourceException("already following"));

        mockMvc.perform(post(BASE + "/{playlistId}/follow", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/v1/playlists/{id}/unfollow success returns 200")
    void unfollow_success_200() throws Exception {
        mockMvc.perform(delete(BASE + "/{playlistId}/unfollow", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(playlistService).unfollowPlaylist(PLAYLIST_ID);
    }

    @Test
    @DisplayName("GET /api/v1/playlists/public returns paged data")
    void getPublicPlaylists_success() throws Exception {
        PagedResponseDto<PlaylistResponse> page = new PagedResponseDto<>(
                List.of(PlaylistResponse.builder().id(PLAYLIST_ID).name("Public").build()),
                0, 10, 1, 1, null, null
        );
        when(playlistService.getPublicPlaylists(0, 10)).thenReturn(page);

        mockMvc.perform(get(BASE + "/public")
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Public"));
    }

    @Test
    @DisplayName("POST /api/v1/playlists/{id}/songs success returns 201")
    void addSong_success_201() throws Exception {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(SONG_ID);
        when(playlistService.addSongToPlaylist(eq(PLAYLIST_ID), any(AddSongToPlaylistRequest.class)))
                .thenReturn(PlaylistSongResponse.builder().songId(SONG_ID).position(1).build());

        mockMvc.perform(post(BASE + "/{playlistId}/songs", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.songId").value(SONG_ID));
    }

    @Test
    @DisplayName("PUT /api/v1/playlists/{id}/songs/reorder success returns 200")
    void reorder_success_200() throws Exception {
        SongPositionRequest item = new SongPositionRequest();
        item.setSongId(1L);
        item.setPosition(1);
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongs(List.of(item));

        when(playlistService.reorderPlaylistSongs(eq(PLAYLIST_ID), any(ReorderPlaylistSongsRequest.class)))
                .thenReturn(List.of(PlaylistSongResponse.builder().songId(1L).position(1).build()));

        mockMvc.perform(put(BASE + "/{playlistId}/songs/reorder", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/playlists/{id}/follow success returns 201")
    void follow_success_201() throws Exception {
        when(playlistService.followPlaylist(PLAYLIST_ID))
                .thenReturn(PlaylistFollowResponse.builder().id(1L).playlistId(PLAYLIST_ID).followerUserId(USER_ID).build());

        mockMvc.perform(post(BASE + "/{playlistId}/follow", PLAYLIST_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    private RequestPostProcessor authUser(Long userId, UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "user-" + userId, role);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
