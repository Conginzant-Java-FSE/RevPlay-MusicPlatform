package com.revplay.musicplatform.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.catalog.dto.request.PodcastCategoryCreateRequest;
import com.revplay.musicplatform.catalog.dto.response.PodcastCategoryResponse;
import com.revplay.musicplatform.catalog.service.PodcastCategoryService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PodcastCategoryController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class PodcastCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private PodcastCategoryService service;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("GET /api/v1/podcast-categories: success")
    void list_Success() throws Exception {
        securityContextHelper.setMockUser(16L, "user", UserRole.LISTENER.name());
        PodcastCategoryResponse response = new PodcastCategoryResponse();
        response.setCategoryId(1L);
        response.setName("Comedy");
        when(service.list()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/podcast-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Comedy"));
    }

    @Test
    @DisplayName("POST /api/v1/podcast-categories: success (ADMIN)")
    void create_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "adminUser", UserRole.ADMIN.name());
        PodcastCategoryCreateRequest request = new PodcastCategoryCreateRequest();
        request.setName("Comedy");

        PodcastCategoryResponse response = new PodcastCategoryResponse();
        response.setCategoryId(1L);
        response.setName("Comedy");
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/podcast-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Comedy"));
    }
}



