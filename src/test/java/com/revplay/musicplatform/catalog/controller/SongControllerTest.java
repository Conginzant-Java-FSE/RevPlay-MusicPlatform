package com.revplay.musicplatform.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.catalog.dto.request.SongCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.SongUpdateRequest;
import com.revplay.musicplatform.catalog.dto.request.SongVisibilityRequest;
import com.revplay.musicplatform.catalog.dto.response.SongResponse;
import com.revplay.musicplatform.catalog.enums.ContentVisibility;
import com.revplay.musicplatform.catalog.service.SongService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(SongController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private SongService songService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("POST /api/v1/songs: success (ARTIST)")
    void create_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        SongCreateRequest request = new SongCreateRequest();
        request.setTitle("New Song");
        request.setAlbumId(10L);

        String metadata = objectMapper.writeValueAsString(request);
        MockMultipartFile metadataPart = new MockMultipartFile("metadata", "", "application/json", metadata.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "test data".getBytes());

        SongResponse response = new SongResponse();
        response.setSongId(100L);
        response.setTitle("New Song");

        when(songService.create(any(SongCreateRequest.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/songs")
                .file(metadataPart)
                .file(filePart)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Song"));
    }

    @Test
    @DisplayName("PUT /api/v1/songs/{id}: success (ARTIST)")
    void update_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        SongUpdateRequest request = new SongUpdateRequest();
        request.setTitle("Updated Title");
        request.setDurationSeconds(200);

        SongResponse response = new SongResponse();
        response.setSongId(100L);
        response.setTitle("Updated Title");

        when(songService.update(eq(100L), any(SongUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/songs/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @DisplayName("GET /api/v1/songs/{id}: success")
    void get_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        SongResponse response = new SongResponse();
        response.setTitle("Faded");

        when(songService.get(100L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/songs/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Faded"));
    }

    @Test
    @DisplayName("DELETE /api/v1/songs/{id}: success (ARTIST)")
    void delete_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        mockMvc.perform(delete("/api/v1/songs/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song deleted"));
    }

    @Test
    @DisplayName("GET /api/v1/artists/{id}/songs: success")
    void listByArtist_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        SongResponse s1 = new SongResponse();
        s1.setTitle("S1");

        when(songService.listByArtist(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(s1)));

        mockMvc.perform(get("/api/v1/artists/10/songs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("S1"));
    }

    @Test
    @DisplayName("PATCH /api/v1/songs/{id}/visibility: success")
    void updateVisibility_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        SongVisibilityRequest request = new SongVisibilityRequest();
        request.setVisibility(ContentVisibility.PUBLIC);
        request.setIsActive(true);

        when(songService.updateVisibility(eq(100L), any())).thenReturn(new SongResponse());

        mockMvc.perform(patch("/api/v1/songs/100/visibility")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/songs/{id}/audio: success")
    void replaceAudio_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        MockMultipartFile filePart = new MockMultipartFile("file", "new.mp3", "audio/mpeg", "new data".getBytes());

        when(songService.replaceAudio(eq(100L), any())).thenReturn(new SongResponse());

        mockMvc.perform(multipart("/api/v1/songs/100/audio")
                .file(filePart)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk());
    }
}



