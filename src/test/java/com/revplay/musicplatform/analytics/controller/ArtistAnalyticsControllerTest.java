package com.revplay.musicplatform.analytics.controller;

import com.revplay.musicplatform.analytics.dto.response.ArtistDashboardResponse;
import com.revplay.musicplatform.analytics.service.ArtistAnalyticsService;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.PlaybackRateLimiterService;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(ArtistAnalyticsController.class)
@Import(SecurityConfig.class)
class ArtistAnalyticsControllerTest {

    private static final Long ARTIST_ID = 12L;
    private static final String DASHBOARD_URL = "/api/v1/analytics/artists/{artistId}/dashboard";

    private final MockMvc mockMvc;

    @MockBean
    private ArtistAnalyticsService artistAnalyticsService;
    @MockBean
    private PlaybackRateLimiterService playbackRateLimiterService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    ArtistAnalyticsControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("artist dashboard returns 200 for artist auth")
    void dashboard_artist_ok() throws Exception {
        when(artistAnalyticsService.dashboard(ARTIST_ID))
                .thenReturn(new ArtistDashboardResponse(ARTIST_ID, 10L, 100L, 50L));

        mockMvc.perform(get(DASHBOARD_URL, ARTIST_ID).with(authentication(auth(UserRole.ARTIST))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.artistId").value(ARTIST_ID));
    }

    @Test
    @DisplayName("artist dashboard listener forbidden when downstream rejects")
    void dashboard_listener_forbidden() throws Exception {
        doThrow(new AccessDeniedException("Artist access required"))
                .when(artistAnalyticsService).dashboard(ARTIST_ID);

        mockMvc.perform(get(DASHBOARD_URL, ARTIST_ID).with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("artist dashboard no auth returns 403")
    void dashboard_noAuth_forbidden() throws Exception {
        mockMvc.perform(get(DASHBOARD_URL, ARTIST_ID))
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
