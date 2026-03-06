package com.revplay.musicplatform.analytics.service.impl;

import com.revplay.musicplatform.analytics.dto.response.*;
import com.revplay.musicplatform.analytics.enums.TimePeriod;
import com.revplay.musicplatform.analytics.service.PlaybackAnalyticsService;
import com.revplay.musicplatform.analytics.service.UserStatisticsService;
import com.revplay.musicplatform.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@SpringBootTest(classes = {PlaybackAnalyticsServiceImpl.class, CacheConfig.class})
class PlaybackAnalyticsServiceImplTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;
    @MockBean
    private UserStatisticsService userStatisticsService;

    private final PlaybackAnalyticsService service;
    private final CacheManager cacheManager;

    @Autowired
    PlaybackAnalyticsServiceImplTest(PlaybackAnalyticsService service, CacheManager cacheManager) {
        this.service = service;
        this.cacheManager = cacheManager;
    }

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("analytics.trending") != null) {
            cacheManager.getCache("analytics.trending").clear();
        }
        if (cacheManager.getCache("analytics.dashboard") != null) {
            cacheManager.getCache("analytics.dashboard").clear();
        }
        reset(jdbcTemplate, userStatisticsService);
    }

    @Test
    @DisplayName("trending DAILY uses now minus one day cutoff")
    @SuppressWarnings("unchecked")
    void trending_daily_cutoffMinusOneDay() {
        Instant[] captured = new Instant[1];
        doAnswer(invocation -> {
            captured[0] = invocation.getArgument(2);
            return List.of();
        }).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), anyInt());

        service.trending("song", TimePeriod.DAILY, 10);

        assertThat(captured[0]).isNotNull();
        assertThat(captured[0]).isBetween(
                Instant.now().minus(2, ChronoUnit.DAYS),
                Instant.now().minus(20, ChronoUnit.HOURS)
        );
    }

    @Test
    @DisplayName("trending WEEKLY uses now minus seven days cutoff")
    @SuppressWarnings("unchecked")
    void trending_weekly_cutoffMinusSevenDays() {
        Instant[] captured = new Instant[1];
        doAnswer(invocation -> {
            captured[0] = invocation.getArgument(2);
            return List.of();
        }).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), anyInt());

        service.trending("song", TimePeriod.WEEKLY, 10);

        assertThat(captured[0]).isBetween(
                Instant.now().minus(8, ChronoUnit.DAYS),
                Instant.now().minus(6, ChronoUnit.DAYS)
        );
    }

    @Test
    @DisplayName("trending MONTHLY uses now minus thirty days cutoff")
    @SuppressWarnings("unchecked")
    void trending_monthly_cutoffMinusThirtyDays() {
        Instant[] captured = new Instant[1];
        doAnswer(invocation -> {
            captured[0] = invocation.getArgument(2);
            return List.of();
        }).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), anyInt());

        service.trending("song", TimePeriod.MONTHLY, 10);

        assertThat(captured[0]).isBetween(
                Instant.now().minus(31, ChronoUnit.DAYS),
                Instant.now().minus(29, ChronoUnit.DAYS)
        );
    }

    @Test
    @DisplayName("trending with no plays returns empty list")
    @SuppressWarnings("unchecked")
    void trending_noPlays_returnsEmpty() {
        doReturn(List.of()).when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), anyInt());

        List<TrendingContentResponse> actual = service.trending("song", TimePeriod.DAILY, 10);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("trending JdbcTemplate failure returns empty list")
    @SuppressWarnings("unchecked")
    void trending_jdbcFailure_returnsEmpty() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), anyInt()))
                .thenThrow(new DataAccessResourceFailureException("db"));

        List<TrendingContentResponse> actual = service.trending("song", TimePeriod.DAILY, 10);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("dashboardMetrics JdbcTemplate failure returns zeroed response")
    void dashboardMetrics_jdbcFailure_zeroed() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any()))
                .thenThrow(new DataAccessResourceFailureException("db"));

        DashboardMetricsResponse actual = service.dashboardMetrics();

        assertThat(actual.totalPlatformPlays()).isZero();
        assertThat(actual.playsLast24Hours()).isZero();
        ActiveUsersMetricsResponse active = actual.activeUsers();
        assertThat(active.last24Hours()).isZero();
        assertThat(active.last7Days()).isZero();
        assertThat(active.last30Days()).isZero();
    }

    @Test
    @DisplayName("userStats with DataAccessException returns empty top genres fallback stats")
    @SuppressWarnings("unchecked")
    void userStats_dataAccessException_fallbacks() {
        UserStatisticsResponse base = new UserStatisticsResponse(1L, 0L, 0L, 0L, 0L, null);
        when(userStatisticsService.getByUserId(1L)).thenReturn(base);
        when(userStatisticsService.refreshAndGet(1L)).thenReturn(base);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenThrow(new DataAccessResourceFailureException("db"));

        UserListeningStatsResponse actual = service.userStats(1L);

        assertThat(actual.baseStatistics()).isEqualTo(base);
        assertThat(actual.topGenres()).isEmpty();
        assertThat(actual.peakListeningHour()).isNull();
    }

    @Test
    @DisplayName("cache hit for trending same key calls JdbcTemplate once")
    @SuppressWarnings("unchecked")
    void trending_sameKey_cacheHit_callsOnce() {
        doReturn(List.of(new TrendingContentResponse("song", 1L, "S", 3L)))
                .when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), anyInt());

        List<TrendingContentResponse> first = service.trending("song", TimePeriod.DAILY, 10);
        List<TrendingContentResponse> second = service.trending("song", TimePeriod.DAILY, 10);

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class), any(), anyInt());
    }

    @Test
    @DisplayName("cache keys differ for different periods")
    @SuppressWarnings("unchecked")
    void trending_differentPeriods_callsTwice() {
        doReturn(List.of(new TrendingContentResponse("song", 1L, "S", 3L)))
                .when(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), anyInt());

        service.trending("song", TimePeriod.DAILY, 10);
        service.trending("song", TimePeriod.WEEKLY, 10);

        verify(jdbcTemplate, times(2)).query(anyString(), any(RowMapper.class), any(), anyInt());
    }
}
