package com.revplay.revplay.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaylistSongCreateRequest {

    @NotNull(message = "Playlist ID is required")
    private Long playlistId;

    @NotNull(message = "Song ID is required")
    private Long songId;
}