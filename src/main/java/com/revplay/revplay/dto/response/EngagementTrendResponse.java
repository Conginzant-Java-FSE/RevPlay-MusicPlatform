package com.revplay.revplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EngagementTrendResponse {

    private String period;
    private List<DataPoint> dataPoints;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDate date;
        private Long playCount;
        private Long uniqueListeners;
        private Double avgCompletionRate;
    }
}
