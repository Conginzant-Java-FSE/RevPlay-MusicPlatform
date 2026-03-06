package com.revplay.musicplatform.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.catalog.dto.request.SongGenresRequest;
import com.revplay.musicplatform.catalog.service.SongGenreService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(SongGenreController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class SongGenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private SongGenreService service;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("POST /api/v1/songs/{id}/genres: success (ARTIST)")
    void assign_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());
        SongGenresRequest request = new SongGenresRequest();
        request.setGenreIds(List.of(1L, 2L));

        mockMvc.perform(post("/api/v1/songs/100/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service).addGenres(eq(100L), anyList());
    }

    @Test
    @DisplayName("PUT /api/v1/songs/{id}/genres: success (ARTIST)")
    void replace_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());
        SongGenresRequest request = new SongGenresRequest();
        request.setGenreIds(List.of(3L));

        mockMvc.perform(put("/api/v1/songs/100/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service).replaceGenres(eq(100L), anyList());
    }
}



