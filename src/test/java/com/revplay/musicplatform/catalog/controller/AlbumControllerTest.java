package com.revplay.musicplatform.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.catalog.dto.request.AlbumCreateRequest;
import com.revplay.musicplatform.catalog.dto.request.AlbumUpdateRequest;
import com.revplay.musicplatform.catalog.dto.response.AlbumResponse;
import com.revplay.musicplatform.catalog.service.AlbumService;
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
@WebMvcTest(AlbumController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private AlbumService albumService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("POST /api/v1/albums: success (ARTIST)")
    void create_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        AlbumCreateRequest request = new AlbumCreateRequest();
        request.setTitle("New Album");

        AlbumResponse response = new AlbumResponse();
        response.setAlbumId(10L);
        response.setTitle("New Album");

        when(albumService.create(any(AlbumCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Album"));
    }

    @Test
    @DisplayName("PUT /api/v1/albums/{id}: success (ARTIST)")
    void update_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        AlbumUpdateRequest request = new AlbumUpdateRequest();
        request.setTitle("Updated Title");

        AlbumResponse response = new AlbumResponse();
        response.setTitle("Updated Title");

        when(albumService.update(eq(10L), any(AlbumUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/albums/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    @DisplayName("GET /api/v1/albums/{id}: success")
    void get_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        AlbumResponse response = new AlbumResponse();
        response.setTitle("Discovery");

        when(albumService.get(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/albums/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Discovery"));
    }

    @Test
    @DisplayName("DELETE /api/v1/albums/{id}: success (ARTIST)")
    void delete_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        mockMvc.perform(delete("/api/v1/albums/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Album deleted"));

        verify(albumService).delete(10L);
    }

    @Test
    @DisplayName("GET /api/v1/artists/{id}/albums: success")
    void listByArtist_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        AlbumResponse a1 = new AlbumResponse();
        a1.setTitle("Album 1");

        when(albumService.listByArtist(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a1)));

        mockMvc.perform(get("/api/v1/artists/1/albums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Album 1"));
    }

    @Test
    @DisplayName("PUT /api/v1/albums/{albumId}/songs/{songId}: success")
    void addSong_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        mockMvc.perform(put("/api/v1/albums/10/songs/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song added to album"));

        verify(albumService).addSongToAlbum(10L, 100L);
    }

    @Test
    @DisplayName("DELETE /api/v1/albums/{albumId}/songs/{songId}: success")
    void removeSong_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());

        mockMvc.perform(delete("/api/v1/albums/10/songs/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song removed from album"));

        verify(albumService).removeSongFromAlbum(10L, 100L);
    }
}



