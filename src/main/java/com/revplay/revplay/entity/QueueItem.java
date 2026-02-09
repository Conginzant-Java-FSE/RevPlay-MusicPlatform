package com.revplay.revplay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_items", indexes = {
        @Index(name = "idx_queue_user_position", columnList = "user_id, position"),
        @Index(name = "idx_queue_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long queueId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "song_id")
    private Long songId;

    @Column(name = "episode_id")
    private Long episodeId;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        validateContentReference();
    }

    @PreUpdate
    protected void onUpdate() {
        validateContentReference();
    }

    private void validateContentReference() {
        if ((songId == null && episodeId == null) || (songId != null && episodeId != null)) {
            throw new IllegalStateException("Exactly one of songId or episodeId must be set");
        }
    }
}

