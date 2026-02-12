package com.revplay.revplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedListeningStatsResponse {

    private Long totalPlays;
    private Long uniqueSongs;
    private Long uniqueEpisodes;
    private Map<Integer, Long> peakHours;

    private Integer avgDurationSeconds;
    private Double completionRate;
    private Long uniqueListeners;
    private Long totalListenTimeSeconds;
    private ContentTypeBreakdown breakdown;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentTypeBreakdown {
        private Long songPlays;
        private Long podcastPlays;
        private Double songCompletionRate;
        private Double podcastCompletionRate;
    }
}
