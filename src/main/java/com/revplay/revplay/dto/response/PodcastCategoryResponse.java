package com.revplay.revplay.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PodcastCategoryResponse {
    private Long categoryId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}

