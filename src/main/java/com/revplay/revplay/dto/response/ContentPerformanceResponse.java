package com.revplay.revplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentPerformanceResponse {

    private Long contentId;
    private String contentType;
    private Long totalPlays;
    private Double completionRate;
    private Long uniqueListeners;
}
