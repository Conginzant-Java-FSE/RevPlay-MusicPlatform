package com.revplay.musicplatform.playlist.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.playlist.dto.request.AddSongToPlaylistRequest;
import com.revplay.musicplatform.playlist.dto.request.CreatePlaylistRequest;
import com.revplay.musicplatform.playlist.dto.request.ReorderPlaylistSongsRequest;
import com.revplay.musicplatform.playlist.dto.request.SongPositionRequest;
import com.revplay.musicplatform.playlist.entity.Playlist;
import com.revplay.musicplatform.playlist.entity.PlaylistSong;
import com.revplay.musicplatform.playlist.repository.PlaylistFollowRepository;
import com.revplay.musicplatform.playlist.repository.PlaylistRepository;
import com.revplay.musicplatform.playlist.repository.PlaylistSongRepository;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlaylistIntegrationTest {

    private static final String BASE = "/api/v1/playlists";
    private static final Long USER_A = 100L;
    private static final Long USER_B = 200L;
    private static final Long SONG_1 = 1001L;
    private static final Long SONG_2 = 1002L;
    private static final Long SONG_3 = 1003L;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistFollowRepository playlistFollowRepository;

    @Autowired
    PlaylistIntegrationTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            PlaylistRepository playlistRepository,
            PlaylistSongRepository playlistSongRepository,
            PlaylistFollowRepository playlistFollowRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.playlistFollowRepository = playlistFollowRepository;
    }

    @BeforeEach
    void clean() {
        playlistFollowRepository.deleteAll();
        playlistSongRepository.deleteAll();
        playlistRepository.deleteAll();
    }

    @Test
    @DisplayName("Create playlist add songs and verify order 1 2 3")
    void createAddSongs_verifyOrder() throws Exception {
        Long playlistId = createPlaylist(USER_A, "Integration A", true);
        addSong(playlistId, SONG_1, USER_A, null);
        addSong(playlistId, SONG_2, USER_A, null);
        addSong(playlistId, SONG_3, USER_A, null);

        List<PlaylistSong> songs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);

        assertThat(songs).hasSize(3);
        assertThat(songs.get(0).getSongId()).isEqualTo(SONG_1);
        assertThat(songs.get(0).getPosition()).isEqualTo(1);
        assertThat(songs.get(1).getPosition()).isEqualTo(2);
        assertThat(songs.get(2).getPosition()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reorder songs and verify persisted positions")
    void reorderSongs_verifyNewOrder() throws Exception {
        Long playlistId = createPlaylist(USER_A, "Integration B", true);
        addSong(playlistId, SONG_1, USER_A, null);
        addSong(playlistId, SONG_2, USER_A, null);
        addSong(playlistId, SONG_3, USER_A, null);

        SongPositionRequest a = new SongPositionRequest();
        a.setSongId(SONG_1);
        a.setPosition(3);
        SongPositionRequest b = new SongPositionRequest();
        b.setSongId(SONG_2);
        b.setPosition(1);
        SongPositionRequest c = new SongPositionRequest();
        c.setSongId(SONG_3);
        c.setPosition(2);
        ReorderPlaylistSongsRequest reorder = new ReorderPlaylistSongsRequest();
        reorder.setSongs(List.of(a, b, c));

        mockMvc.perform(put(BASE + "/{playlistId}/songs/reorder", playlistId)
                        .with(authUser(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorder)))
                .andExpect(status().isOk());

        List<PlaylistSong> songs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);
        assertThat(songs).hasSize(3);
        assertThat(songs.get(0).getSongId()).isEqualTo(SONG_2);
        assertThat(songs.get(1).getSongId()).isEqualTo(SONG_3);
        assertThat(songs.get(2).getSongId()).isEqualTo(SONG_1);
    }

    @Test
    @DisplayName("Add duplicate song returns 409")
    void addDuplicateSong_conflict() throws Exception {
        Long playlistId = createPlaylist(USER_A, "Integration C", true);
        addSong(playlistId, SONG_1, USER_A, null);

        AddSongToPlaylistRequest duplicate = new AddSongToPlaylistRequest();
        duplicate.setSongId(SONG_1);

        MvcResult result = mockMvc.perform(post(BASE + "/{playlistId}/songs", playlistId)
                        .with(authUser(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("Follow public playlist as another user succeeds")
    void followPublicPlaylist_success() throws Exception {
        Long playlistId = createPlaylist(USER_A, "Integration D", true);

        mockMvc.perform(post(BASE + "/{playlistId}/follow", playlistId)
                        .with(authUser(USER_B)))
                .andExpect(status().isCreated());

        assertThat(playlistFollowRepository.existsByPlaylistIdAndFollowerUserId(playlistId, USER_B)).isTrue();
    }

    @Test
    @DisplayName("Follow own playlist returns 409")
    void followOwnPlaylist_conflict() throws Exception {
        Long playlistId = createPlaylist(USER_A, "Integration E", true);

        MvcResult result = mockMvc.perform(post(BASE + "/{playlistId}/follow", playlistId)
                        .with(authUser(USER_A)))
                .andExpect(status().isConflict())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("Follow private playlist as non owner returns 403")
    void followPrivatePlaylist_nonOwner_forbidden() throws Exception {
        Long playlistId = createPlaylist(USER_A, "Integration F", false);

        MvcResult result = mockMvc.perform(post(BASE + "/{playlistId}/follow", playlistId)
                        .with(authUser(USER_B)))
                .andExpect(status().isForbidden())
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Delete playlist then get returns 404")
    void deleteThenGet_notFound() throws Exception {
        Long playlistId = createPlaylist(USER_A, "Integration G", true);

        mockMvc.perform(delete(BASE + "/{playlistId}", playlistId)
                        .with(authUser(USER_A)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE + "/{playlistId}", playlistId)
                        .with(authUser(USER_A)))
                .andExpect(status().isNotFound());

        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        assertThat(playlist.getIsActive()).isFalse();
    }

    private Long createPlaylist(Long userId, String name, boolean isPublic) throws Exception {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName(name);
        request.setIsPublic(isPublic);

        MvcResult result = mockMvc.perform(post(BASE)
                        .with(authUser(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("data").path("id").asLong();
    }

    private void addSong(Long playlistId, Long songId, Long userId, Integer position) throws Exception {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(songId);
        request.setPosition(position);

        mockMvc.perform(post(BASE + "/{playlistId}/songs", playlistId)
                        .with(authUser(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private RequestPostProcessor authUser(Long userId) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "user-" + userId, UserRole.LISTENER);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
