package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaylistFollowCreateRequest {

    @NotNull(message = "Playlist ID is required")
    private Long playlistId;

    @NotNull(message = "User ID is required")
    private Long userId;
}