package com.revplay.revplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatorPerformanceResponse {

    private Long creatorId;
    private String creatorName;
    private Long totalPlays;
    private Long uniqueListeners;
    private Double avgCompletionRate;
    private Integer avgListenDurationSeconds;
    private Integer totalContent;
}
