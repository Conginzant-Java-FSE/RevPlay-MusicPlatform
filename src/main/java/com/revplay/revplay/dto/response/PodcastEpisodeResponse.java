package com.revplay.revplay.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PodcastEpisodeResponse {

    private Long episodeId;
    private Long podcastId;

    private String title;
    private String audioUrl;
    private Integer durationSeconds;

    private LocalDate releaseDate;
}
