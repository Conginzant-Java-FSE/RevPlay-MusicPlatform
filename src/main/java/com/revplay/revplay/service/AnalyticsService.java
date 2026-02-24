package com.revplay.revplay.service;

import com.revplay.revplay.dto.response.*;
import com.revplay.revplay.enums.TimePeriod;

import java.util.List;

public interface AnalyticsService {

    List<TopContentResponse> getTopSongs(Long userId, TimePeriod period);

    List<TopContentResponse> getTopEpisodes(Long userId, TimePeriod period);

    List<TopContentResponse> getTrendingSongs(TimePeriod period);

    List<TopContentResponse> getTrendingEpisodes(TimePeriod period);

    EnhancedListeningStatsResponse getEnhancedListeningStats(Long userId,
                                                             TimePeriod period);

    CompletionMetricsResponse getSongCompletionMetrics(Long songId,
                                                       TimePeriod period);

    ContentComparisonResponse compareContentTypes(TimePeriod period);

    EngagementTrendResponse getEngagementTrends(TimePeriod period);

    DashboardAnalyticsResponse getDashboardAnalytics(TimePeriod period);
}
