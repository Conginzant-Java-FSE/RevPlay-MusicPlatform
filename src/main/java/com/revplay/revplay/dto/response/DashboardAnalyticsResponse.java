package com.revplay.revplay.dto.response;

import com.revplay.revplay.dto.response.TopContentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsResponse {

    private OverviewMetrics overview;
    private ContentComparisonResponse.ContentTypeMetrics songs;
    private ContentComparisonResponse.ContentTypeMetrics podcasts;
    private Map<Integer, Long> peakHours;
    private List<CreatorPerformanceResponse> topCreators;
    private List<TopContentResponse> topSongs;
    private List<TopContentResponse> topPodcasts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewMetrics {
        private Long totalPlays;
        private Long uniqueListeners;
        private Double avgCompletionRate;
        private Integer avgDurationSeconds;
    }
}
