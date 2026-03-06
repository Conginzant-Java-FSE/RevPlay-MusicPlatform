package com.revplay.musicplatform.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.catalog.dto.request.PodcastCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.PodcastUpdateRequest;
import com.revplay.musicplatform.catalog.dto.response.PodcastResponse;
import com.revplay.musicplatform.catalog.service.PodcastService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PodcastController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class PodcastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private PodcastService podcastService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("POST /api/v1/podcasts: success (ARTIST)")
    void create_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        PodcastCreateRequest request = new PodcastCreateRequest();
        request.setCategoryId(1L);
        request.setTitle("New Podcast");

        PodcastResponse response = new PodcastResponse();
        response.setPodcastId(10L);
        response.setTitle("New Podcast");

        when(podcastService.create(any(PodcastCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/podcasts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("New Podcast"));
    }

    @Test
    @DisplayName("PUT /api/v1/podcasts/{id}: success (ARTIST)")
    void update_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        PodcastUpdateRequest request = new PodcastUpdateRequest();
        request.setCategoryId(1L);
        request.setTitle("Updated Title");

        PodcastResponse response = new PodcastResponse();
        response.setTitle("Updated Title");

        when(podcastService.update(eq(10L), any(PodcastUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/podcasts/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @DisplayName("GET /api/v1/podcasts/{id}: success")
    void get_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        PodcastResponse response = new PodcastResponse();
        response.setTitle("Test Podcast");

        when(podcastService.get(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/podcasts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Podcast"));
    }

    @Test
    @DisplayName("DELETE /api/v1/podcasts/{id}: success (ARTIST)")
    void delete_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        mockMvc.perform(delete("/api/v1/podcasts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Podcast deleted"));

        verify(podcastService).delete(10L);
    }

    @Test
    @DisplayName("GET /api/v1/podcasts/recommended: success")
    void listRecommended_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        PodcastResponse p1 = new PodcastResponse();
        p1.setTitle("Recommended Podcast");

        when(podcastService.listRecommended(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p1)));

        mockMvc.perform(get("/api/v1/podcasts/recommended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Recommended Podcast"));
    }
}




