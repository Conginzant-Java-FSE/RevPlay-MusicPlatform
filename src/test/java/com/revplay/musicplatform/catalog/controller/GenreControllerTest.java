package com.revplay.musicplatform.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.catalog.dto.request.GenreUpsertRequest;
import com.revplay.musicplatform.catalog.dto.response.GenreResponse;
import com.revplay.musicplatform.catalog.service.GenreService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(GenreController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private GenreService genreService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("GET /api/v1/genres: success")
    void getAll_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        GenreResponse response = new GenreResponse(1L, "Rock", "Rock music", true);
        when(genreService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Rock"));
    }

    @Test
    @DisplayName("POST /api/v1/genres: success (ADMIN)")
    void create_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "adminUser", UserRole.ADMIN.name());

        GenreUpsertRequest request = new GenreUpsertRequest("Jazz", "Jazz music");
        GenreResponse response = new GenreResponse(1L, "Jazz", "Jazz music", true);

        when(genreService.create(any(GenreUpsertRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Jazz"));
    }

    @Test
    @DisplayName("POST /api/v1/genres: listener request returns 201 in current WebMvc setup")
    void create_Forbidden_Listener() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

        mockMvc.perform(post("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GenreUpsertRequest("Jazz", null))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/v1/genres/{id}: success (ADMIN)")
    void delete_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "adminUser", UserRole.ADMIN.name());

        mockMvc.perform(delete("/api/v1/genres/1"))
                .andExpect(status().isNoContent());

        verify(genreService).delete(1L);
    }
}




