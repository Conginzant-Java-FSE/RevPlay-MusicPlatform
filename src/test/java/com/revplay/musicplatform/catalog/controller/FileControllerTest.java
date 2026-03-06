package com.revplay.musicplatform.catalog.controller;

import com.revplay.musicplatform.catalog.util.FileStorageService;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@WebMvcTest(FileController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean

    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @MockBean
    private FileStorageProperties fileStorageProperties;


    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private TokenRevocationServiceImpl tokenRevocationService;

    @Autowired
    private MockSecurityContextHelper securityContextHelper;

    @Test
    @DisplayName("GET /api/v1/files/songs/{fileName}: success")
    void getSong_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());
        Resource resource = new ByteArrayResource("test audio".getBytes());
        when(fileStorageService.loadSong("test.mp3")).thenReturn(resource);

        mockMvc.perform(get("/api/v1/files/songs/test.mp3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.mp3\""));
    }

    @Test
    @DisplayName("GET /api/v1/files/images/{fileName}: success")
    void getImage_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());
        Resource resource = new ByteArrayResource("test image".getBytes());
        when(fileStorageService.loadImage("test.jpg")).thenReturn(resource);

        mockMvc.perform(get("/api/v1/files/images/test.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.jpg\""));
    }

    @Test
    @DisplayName("POST /api/v1/files/images: success")
    void uploadImage_Success() throws Exception {
        securityContextHelper.setMockUser(1L, "artistUser", UserRole.ARTIST.name());
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image".getBytes());
        when(fileStorageService.storeImage(any())).thenReturn("stored.jpg");

        mockMvc.perform(multipart("/api/v1/files/images").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageUrl").value("/api/v1/files/images/stored.jpg"));
    }
}



