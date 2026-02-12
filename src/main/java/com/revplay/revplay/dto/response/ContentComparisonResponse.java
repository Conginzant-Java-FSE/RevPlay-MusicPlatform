package com.revplay.revplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentComparisonResponse {

    private ContentTypeMetrics songs;
    private ContentTypeMetrics podcasts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentTypeMetrics {
        private Long totalPlays;
        private Long uniqueListeners;
        private Integer avgDurationSeconds;
        private Double completionRate;
    }
}
