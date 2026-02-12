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
public class ListeningStatsResponse {

    private Long totalPlays;
    private Long uniqueSongs;
    private Long uniqueEpisodes;
    private Map<Integer, Long> peakHours;
}
