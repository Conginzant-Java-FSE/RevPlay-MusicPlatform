package com.revplay.revplay.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PodcastResponse {

    private Long podcastId;
    private Long artistId;
    private Long categoryId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}

