package com.revplay.revplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {

    private Long totalPlatformPlays;
    private Long activeUsersCount;
    private Long totalSongsPlayed;
    private Long totalEpisodesPlayed;
}

