package com.revplay.musicplatform.analytics.controller;

import com.revplay.musicplatform.analytics.dto.response.BusinessOverviewResponse;
import com.revplay.musicplatform.analytics.service.AdminBusinessAnalyticsService;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.AccessDeniedException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AdminBusinessAnalyticsController.class)
@Import(SecurityConfig.class)
class AdminBusinessAnalyticsControllerTest {

    private static final String OVERVIEW_URL = "/api/v1/admin/business-analytics/overview";

    private final MockMvc mockMvc;

    @MockBean
    private AdminBusinessAnalyticsService adminBusinessAnalyticsService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    AdminBusinessAnalyticsControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("admin business overview returns 200 for admin auth")
    void overview_admin_ok() throws Exception {
        when(adminBusinessAnalyticsService.getBusinessOverview())
                .thenReturn(new BusinessOverviewResponse(10, 2, 3, 4, 5));

        mockMvc.perform(get(OVERVIEW_URL).with(authentication(auth(UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(10));
    }

    @Test
    @DisplayName("admin business overview returns 403 for listener when service rejects")
    void overview_listener_forbidden() throws Exception {
        when(adminBusinessAnalyticsService.getBusinessOverview())
                .thenThrow(new AccessDeniedException("Admin access required"));

        mockMvc.perform(get(OVERVIEW_URL).with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("admin business overview no auth returns 403")
    void overview_noAuth_forbidden() throws Exception {
        mockMvc.perform(get(OVERVIEW_URL))
                .andExpect(status().isForbidden());
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
