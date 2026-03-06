package com.revplay.musicplatform.analytics.service.impl;

import com.revplay.musicplatform.ads.repository.AdImpressionRepository;
import com.revplay.musicplatform.analytics.dto.response.BusinessOverviewResponse;
import com.revplay.musicplatform.analytics.dto.response.ConversionRateResponse;
import com.revplay.musicplatform.analytics.dto.response.RevenueAnalyticsResponse;
import com.revplay.musicplatform.download.repository.SongDownloadRepository;
import com.revplay.musicplatform.playback.repository.PlayHistoryRepository;
import com.revplay.musicplatform.premium.repository.SubscriptionPaymentRepository;
import com.revplay.musicplatform.premium.repository.UserSubscriptionRepository;
import com.revplay.musicplatform.systemplaylist.repository.SystemPlaylistRepository;
import com.revplay.musicplatform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdminBusinessAnalyticsServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock
    private AdImpressionRepository adImpressionRepository;
    @Mock
    private SongDownloadRepository songDownloadRepository;
    @Mock
    private PlayHistoryRepository playHistoryRepository;
    @Mock
    private SystemPlaylistRepository systemPlaylistRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AdminBusinessAnalyticsServiceImpl service;

    @Test
    @DisplayName("getBusinessOverview returns aggregated counts")
    void getBusinessOverview_returnsAggregates() {
        when(userRepository.count()).thenReturn(100L);
        when(adImpressionRepository.count()).thenReturn(300L);
        when(songDownloadRepository.count()).thenReturn(25L);
        when(playHistoryRepository.count()).thenReturn(999L);
        when(jdbcTemplate.queryForObject(eq("""
            SELECT COUNT(DISTINCT us.user_id)
            FROM user_subscriptions us
            WHERE us.status = 'ACTIVE' AND us.end_date > ?
            """), eq(Long.class), any())).thenReturn(20L);

        BusinessOverviewResponse response = service.getBusinessOverview();

        assertThat(response.totalUsers()).isEqualTo(100L);
        assertThat(response.activePremiumUsers()).isEqualTo(20L);
        assertThat(response.totalSongPlays()).isEqualTo(999L);
    }

    @Test
    @DisplayName("getRevenueAnalytics returns monthly yearly and total revenue")
    void getRevenueAnalytics_returnsRevenue() {
        when(jdbcTemplate.queryForObject(eq("""
            SELECT COALESCE(SUM(sp.amount), 0)
            FROM subscription_payments sp
            WHERE sp.payment_status = 'SUCCESS' AND sp.paid_at >= ? AND sp.paid_at < ?
            """), eq(Double.class), any(), any())).thenReturn(1200.0, 8500.0);
        when(jdbcTemplate.queryForObject(eq("""
            SELECT COALESCE(SUM(sp.amount), 0)
            FROM subscription_payments sp
            WHERE sp.payment_status = 'SUCCESS'
            """), eq(Double.class))).thenReturn(15000.0);

        RevenueAnalyticsResponse response = service.getRevenueAnalytics();

        assertThat(response.monthlyRevenue()).isEqualTo(1200.0);
        assertThat(response.yearlyRevenue()).isEqualTo(8500.0);
        assertThat(response.totalRevenue()).isEqualTo(15000.0);
    }

    @Test
    @DisplayName("getPremiumConversionRate computes percentage and handles zero users")
    void getPremiumConversionRate_handlesZeroUsers() {
        when(userRepository.count()).thenReturn(0L);
        when(jdbcTemplate.queryForObject(eq("""
            SELECT COUNT(DISTINCT us.user_id)
            FROM user_subscriptions us
            WHERE us.status = 'ACTIVE' AND us.end_date > ?
            """), eq(Long.class), any())).thenReturn(0L);

        ConversionRateResponse zeroUsers = service.getPremiumConversionRate();
        assertThat(zeroUsers.percentage()).isZero();

        when(userRepository.count()).thenReturn(200L);
        when(jdbcTemplate.queryForObject(eq("""
            SELECT COUNT(DISTINCT us.user_id)
            FROM user_subscriptions us
            WHERE us.status = 'ACTIVE' AND us.end_date > ?
            """), eq(Long.class), any())).thenReturn(25L);

        ConversionRateResponse response = service.getPremiumConversionRate();
        assertThat(response.percentage()).isEqualTo(12.5);
    }
}
