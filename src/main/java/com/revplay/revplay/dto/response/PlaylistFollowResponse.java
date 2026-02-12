package com.revplay.revplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlaylistFollowResponse {

    private Long id;
    private Long playlistId;
    private Long userId;
    private LocalDateTime followedAt;
}