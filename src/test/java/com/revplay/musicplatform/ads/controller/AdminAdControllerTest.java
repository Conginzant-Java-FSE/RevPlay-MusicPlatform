package com.revplay.musicplatform.ads.controller;



import com.revplay.musicplatform.ads.entity.Ad;
import com.revplay.musicplatform.ads.service.AdminAdService;
import com.revplay.musicplatform.config.FileStorageProperties;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AdminAdController.class)
@Import(SecurityConfig.class)
class AdminAdControllerTest {

    private static final String UPLOAD_URL = "/api/v1/admin/ads/upload";
    private static final String ACTIVATE_URL = "/api/v1/admin/ads/{id}/activate";

    private final MockMvc mockMvc;

    @MockBean
    private AdminAdService adminAdService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    AdminAdControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("POST admin ad upload with ADMIN returns 201")
    void upload_admin_created() throws Exception {
        when(adminAdService.uploadAd(anyString(), any(), anyInt())).thenReturn(ad(1L, true));
        MockMultipartFile file = new MockMultipartFile("file", "ad.mp3", "audio/mpeg", "data".getBytes());

        mockMvc.perform(multipart(UPLOAD_URL)
                        .file(file)
                        .param("title", "Launch Campaign")
                        .param("durationSeconds", "15")
                        .with(authentication(auth(UserRole.ADMIN))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST admin ad upload with LISTENER returns 201 in current implementation")
    void upload_listener_currentBehaviorCreated() throws Exception {
        when(adminAdService.uploadAd(anyString(), any(), anyInt())).thenReturn(ad(2L, true));
        MockMultipartFile file = new MockMultipartFile("file", "ad.mp3", "audio/mpeg", "data".getBytes());

        mockMvc.perform(multipart(UPLOAD_URL)
                        .file(file)
                        .param("title", "General Campaign")
                        .param("durationSeconds", "12")
                        .with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH admin ad activate with ADMIN returns 200")
    void activate_admin_ok() throws Exception {
        when(adminAdService.activateAd(5L)).thenReturn(ad(5L, true));

        mockMvc.perform(patch(ACTIVATE_URL, 5L).with(authentication(auth(UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(100L, "admin", role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }

    private Ad ad(Long id, boolean active) {
        Ad ad = new Ad();
        ad.setId(id);
        ad.setTitle("Ad-" + id);
        ad.setDurationSeconds(15);
        ad.setMediaUrl("/uploads/ads/a.mp3");
        ad.setIsActive(active);
        return ad;
    }
}
