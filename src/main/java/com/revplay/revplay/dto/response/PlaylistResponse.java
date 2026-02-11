package com.revplay.revplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlaylistResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}