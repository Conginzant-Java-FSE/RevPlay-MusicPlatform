package com.revplay.musicplatform.analytics.controller;

import com.revplay.musicplatform.analytics.dto.response.ActiveUsersMetricsResponse;
import com.revplay.musicplatform.analytics.dto.response.ContentPerformanceResponse;
import com.revplay.musicplatform.analytics.dto.response.DashboardMetricsResponse;
import com.revplay.musicplatform.analytics.dto.response.TrendingContentResponse;
import com.revplay.musicplatform.analytics.enums.TimePeriod;
import com.revplay.musicplatform.analytics.service.PlaybackAnalyticsService;
import com.revplay.musicplatform.config.FileStorageProperties;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PlaybackAnalyticsController.class)
@Import(SecurityConfig.class)
class PlaybackAnalyticsControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private PlaybackAnalyticsService playbackAnalyticsService;
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
    PlaybackAnalyticsControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET /api/v1/analytics/dashboard-metrics authenticated returns 200")
    void dashboardMetrics_authenticated_200() throws Exception {
        DashboardMetricsResponse response = new DashboardMetricsResponse(
                10L,
                2L,
                new ActiveUsersMetricsResponse(1L, 2L, 3L),
                new ContentPerformanceResponse(
                        new TrendingContentResponse("song", 1L, "S", 10L),
                        new TrendingContentResponse("podcast", 2L, "P", 9L)
                )
        );
        when(playbackAnalyticsService.dashboardMetrics()).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/dashboard-metrics").with(authUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPlatformPlays").value(10))
                .andExpect(jsonPath("$.data.activeUsers.last24Hours").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/dashboard-metrics no auth returns forbidden")
    void dashboardMetrics_noAuth_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard-metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/analytics/trending authenticated returns 200")
    void trending_authenticated_200() throws Exception {
        when(playbackAnalyticsService.trending(eq("song"), eq(TimePeriod.WEEKLY), eq(5)))
                .thenReturn(List.of(new TrendingContentResponse("song", 1L, "Song X", 3L)));

        mockMvc.perform(get("/api/v1/analytics/trending")
                        .with(authUser())
                        .param("type", "song")
                        .param("period", "WEEKLY")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Song X"));

        verify(playbackAnalyticsService).trending("song", TimePeriod.WEEKLY, 5);
    }

    private RequestPostProcessor authUser() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "u1", UserRole.LISTENER);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
