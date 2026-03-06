package com.revplay.musicplatform.systemplaylist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.systemplaylist.dto.request.AddSystemPlaylistSongsRequest;
import com.revplay.musicplatform.systemplaylist.dto.response.SystemPlaylistResponse;
import com.revplay.musicplatform.systemplaylist.service.SystemPlaylistService;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(SystemPlaylistController.class)
@Import(SecurityConfig.class)
class SystemPlaylistControllerTest {

    private static final String SLUG = "telugu-mix";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private SystemPlaylistService systemPlaylistService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    SystemPlaylistControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("GET system playlists without JWT returns 403")
    void getAll_noJwt_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/system-playlists"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET system playlists authenticated returns 200")
    void getAll_authenticated_returns200() throws Exception {
        SystemPlaylistResponse response = SystemPlaylistResponse.builder()
                .id(1L)
                .name("Telugu Mix")
                .slug(SLUG)
                .description("desc")
                .build();
        when(systemPlaylistService.getAllActivePlaylists()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/system-playlists")
                        .with(authentication(auth(2L, "listener", UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value(SLUG));
    }

    @Test
    @DisplayName("GET system playlist songs with valid slug and auth returns 200")
    void getSongsBySlug_valid_returns200() throws Exception {
        when(systemPlaylistService.getSongIdsBySlug(SLUG)).thenReturn(List.of(11L, 22L));

        mockMvc.perform(get("/api/v1/system-playlists/{slug}/songs", SLUG)
                        .with(authentication(auth(2L, "listener", UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value(11));
    }

    @Test
    @DisplayName("GET system playlist songs with missing slug and auth returns 404")
    void getSongsBySlug_notFound_returns404() throws Exception {
        when(systemPlaylistService.getSongIdsBySlug(SLUG)).thenThrow(new ResourceNotFoundException("System playlist", SLUG));

        mockMvc.perform(get("/api/v1/system-playlists/{slug}/songs", SLUG)
                        .with(authentication(auth(2L, "listener", UserRole.LISTENER))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST add songs with authenticated admin returns 201")
    void addSongs_admin_returns201() throws Exception {
        AddSystemPlaylistSongsRequest request = new AddSystemPlaylistSongsRequest(List.of(11L, 22L));
        doNothing().when(systemPlaylistService).addSongsBySlug(eq(SLUG), anyList());

        mockMvc.perform(post("/api/v1/system-playlists/{slug}/songs", SLUG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(auth(1L, "admin", UserRole.ADMIN))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST add songs with authenticated listener returns 201")
    void addSongs_listener_returns201() throws Exception {
        AddSystemPlaylistSongsRequest request = new AddSystemPlaylistSongsRequest(List.of(11L));
        doNothing().when(systemPlaylistService).addSongsBySlug(eq(SLUG), anyList());

        mockMvc.perform(post("/api/v1/system-playlists/{slug}/songs", SLUG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(auth(2L, "listener", UserRole.LISTENER))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST add songs without JWT returns 403")
    void addSongs_noJwt_returns403() throws Exception {
        AddSystemPlaylistSongsRequest request = new AddSystemPlaylistSongsRequest(List.of(11L));

        mockMvc.perform(post("/api/v1/system-playlists/{slug}/songs", SLUG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private UsernamePasswordAuthenticationToken auth(Long userId, String username, UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, username, role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
