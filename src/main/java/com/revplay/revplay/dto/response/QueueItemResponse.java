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
public class QueueItemResponse {

    private Long queueId;
    private Long userId;
    private Long songId;
    private Long episodeId;
    private Integer position;
    private LocalDateTime createdAt;
    private String contentType;

    public QueueItemResponse(Long queueId, Long userId, Long songId, Long episodeId,
                             Integer position, LocalDateTime createdAt) {
        this.queueId = queueId;
        this.userId = userId;
        this.songId = songId;
        this.episodeId = episodeId;
        this.position = position;
        this.createdAt = createdAt;
        this.contentType = songId != null ? "SONG" : "EPISODE";
    }
}

