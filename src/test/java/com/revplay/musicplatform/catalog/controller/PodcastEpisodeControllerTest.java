package com.revplay.musicplatform.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.catalog.dto.request.PodcastEpisodeCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastEpisodeUpdateRequest;
import com.revplay.musicplatform.catalog.dto.response.PodcastEpisodeResponse;
import com.revplay.musicplatform.catalog.service.PodcastEpisodeService;
import com.revplay.musicplatform.common.MockSecurityContextHelper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.security.service.impl.JwtServiceImpl;
import com.revplay.musicplatform.security.service.impl.TokenRevocationServiceImpl;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PodcastEpisodeController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class PodcastEpisodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private PodcastEpisodeService episodeService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("POST /api/v1/podcasts/{id}/episodes: success (ARTIST)")
    void create_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        PodcastEpisodeCreateRequest request = new PodcastEpisodeCreateRequest();
        request.setTitle("New Episode");

        String metadata = objectMapper.writeValueAsString(request);
        MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json", metadata.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "episode.mp3", "audio/mpeg", "test data".getBytes());

        PodcastEpisodeResponse response = new PodcastEpisodeResponse();
        response.setEpisodeId(1000L);
        response.setTitle("New Episode");

        when(episodeService.create(eq(10L), any(PodcastEpisodeCreateRequest.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/podcasts/10/episodes")
                .file(metadataPart)
                .file(filePart)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("New Episode"));
    }

    @Test
    @DisplayName("PUT /api/v1/podcasts/{pid}/episodes/{eid}: success (ARTIST)")
    void update_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        PodcastEpisodeUpdateRequest request = new PodcastEpisodeUpdateRequest();
        request.setTitle("Updated Title");
        request.setDurationSeconds(1200);

        PodcastEpisodeResponse response = new PodcastEpisodeResponse();
        response.setTitle("Updated Title");

        when(episodeService.update(eq(10L), eq(1000L), any(PodcastEpisodeUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/podcasts/10/episodes/1000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @DisplayName("GET /api/v1/podcasts/{pid}/episodes/{eid}: success")
    void get_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        PodcastEpisodeResponse response = new PodcastEpisodeResponse();
        response.setTitle("Episode 1");

        when(episodeService.get(10L, 1000L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/podcasts/10/episodes/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Episode 1"));
    }

    @Test
    @DisplayName("DELETE /api/v1/podcasts/{pid}/episodes/{eid}: success (ARTIST)")
    void delete_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        mockMvc.perform(delete("/api/v1/podcasts/10/episodes/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Episode deleted"));

        verify(episodeService).delete(10L, 1000L);
    }

    @Test
    @DisplayName("PUT /api/v1/podcasts/{pid}/episodes/{eid}/audio: success")
    void replaceAudio_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        MockMultipartFile filePart = new MockMultipartFile("file", "new.mp3", "audio/mpeg", "new data".getBytes());

        when(episodeService.replaceAudio(eq(10L), eq(1000L), any())).thenReturn(new PodcastEpisodeResponse());

        mockMvc.perform(multipart("/api/v1/podcasts/10/episodes/1000/audio")
                .file(filePart)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk());
    }
}




