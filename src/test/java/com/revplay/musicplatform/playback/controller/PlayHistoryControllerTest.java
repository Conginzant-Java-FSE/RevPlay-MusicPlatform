package com.revplay.musicplatform.playback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.playback.dto.request.TrackPlayRequest;
import com.revplay.musicplatform.playback.dto.response.PlayHistoryResponse;
import com.revplay.musicplatform.playback.service.PlayHistoryService;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PlayHistoryController.class)
@Import(SecurityConfig.class)
class PlayHistoryControllerTest {

    private static final String BASE = "/api/v1/play-history";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private PlayHistoryService playHistoryService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    PlayHistoryControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST /api/v1/play-history/track authenticated returns 201")
    void trackPlay_authenticated_201() throws Exception {
        TrackPlayRequest request = new TrackPlayRequest(1L, 10L, null, true, 120, Instant.now());

        mockMvc.perform(post(BASE + "/track")
                        .with(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(playHistoryService).trackPlay(request);
    }

    @Test
    @DisplayName("POST /api/v1/play-history/track no auth returns forbidden")
    void trackPlay_noAuth_forbidden() throws Exception {
        TrackPlayRequest request = new TrackPlayRequest(1L, 10L, null, true, 120, Instant.now());

        mockMvc.perform(post(BASE + "/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/play-history/{userId} authenticated returns 200")
    void getHistory_authenticated_200() throws Exception {
        when(playHistoryService.getHistory(1L))
                .thenReturn(List.of(new PlayHistoryResponse(1L, 1L, 10L, null, Instant.now(), true, 20)));

        mockMvc.perform(get(BASE + "/{userId}", 1L).with(authUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].playId").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/play-history/{userId} authenticated returns 204")
    void clearHistory_authenticated_204() throws Exception {
        when(playHistoryService.clearHistory(1L)).thenReturn(2L);

        mockMvc.perform(delete(BASE + "/{userId}", 1L).with(authUser()))
                .andExpect(status().isNoContent());

        verify(playHistoryService).clearHistory(1L);
    }

    private RequestPostProcessor authUser() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "u1", UserRole.LISTENER);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
