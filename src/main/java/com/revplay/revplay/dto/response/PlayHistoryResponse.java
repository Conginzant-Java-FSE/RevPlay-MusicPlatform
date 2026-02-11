package com.revplay.revplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayHistoryResponse {

    private Long playId;
    private Long userId;
    private Long songId;
    private Long episodeId;
    private LocalDateTime playedAt;
    private Boolean completed;
    private String contentType;

    public PlayHistoryResponse(Long playId, Long userId, Long songId, Long episodeId,
                               LocalDateTime playedAt, Boolean completed) {
        this.playId = playId;
        this.userId = userId;
        this.songId = songId;
        this.episodeId = episodeId;
        this.playedAt = playedAt;
        this.completed = completed;
        this.contentType = songId != null ? "SONG" : "EPISODE";
    }
}

