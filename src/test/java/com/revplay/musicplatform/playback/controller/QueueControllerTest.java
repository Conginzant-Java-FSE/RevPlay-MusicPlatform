package com.revplay.musicplatform.playback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.playback.dto.request.QueueAddRequest;
import com.revplay.musicplatform.playback.dto.request.QueueReorderRequest;
import com.revplay.musicplatform.playback.dto.response.QueueItemResponse;
import com.revplay.musicplatform.playback.exception.PlaybackNotFoundException;
import com.revplay.musicplatform.playback.service.QueueService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(QueueController.class)
@Import(SecurityConfig.class)
class QueueControllerTest {

    private static final String BASE = "/api/v1/queue";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private QueueService queueService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    QueueControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST /api/v1/queue authenticated valid returns 201")
    void addToQueue_valid_authenticated_201() throws Exception {
        QueueAddRequest request = new QueueAddRequest(1L, 10L, null);
        when(queueService.addToQueue(any(QueueAddRequest.class)))
                .thenReturn(new QueueItemResponse(1L, 1L, 10L, null, 1, Instant.now()));

                mockMvc.perform(post(BASE)
                        .with(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.queueId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/queue both songId and episodeId returns 400")
    void addToQueue_bothIds_badRequest() throws Exception {
        QueueAddRequest request = new QueueAddRequest(1L, 10L, 20L);

        mockMvc.perform(post(BASE)
                        .with(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/queue no auth returns forbidden")
    void addToQueue_noAuth_forbidden() throws Exception {
        QueueAddRequest request = new QueueAddRequest(1L, 10L, null);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/queue/{userId} authenticated returns 200")
    void getQueue_authenticated_200() throws Exception {
        when(queueService.getQueue(1L)).thenReturn(List.of(new QueueItemResponse(1L, 1L, 10L, null, 1, Instant.now())));

        mockMvc.perform(get(BASE + "/{userId}", 1L).with(authUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].queueId").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/queue/{queueId} authenticated returns 204")
    void removeFromQueue_authenticated_204() throws Exception {
        mockMvc.perform(delete(BASE + "/{queueId}", 1L).with(authUser()))
                .andExpect(status().isNoContent());

        verify(queueService).removeFromQueue(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/queue/{queueId} not found returns 404")
    void removeFromQueue_notFound_404() throws Exception {
        doThrow(new PlaybackNotFoundException("missing")).when(queueService).removeFromQueue(1L);

        mockMvc.perform(delete(BASE + "/{queueId}", 1L).with(authUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/queue/reorder valid returns 200")
    void reorder_valid_200() throws Exception {
        QueueReorderRequest request = new QueueReorderRequest(1L, List.of(2L, 1L));
        when(queueService.reorder(eq(request))).thenReturn(List.of(
                new QueueItemResponse(2L, 1L, 20L, null, 1, Instant.now()),
                new QueueItemResponse(1L, 1L, 10L, null, 2, Instant.now())
        ));

        mockMvc.perform(put(BASE + "/reorder")
                        .with(authUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].position").value(1));
    }

    private RequestPostProcessor authUser() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "u1", UserRole.LISTENER);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
