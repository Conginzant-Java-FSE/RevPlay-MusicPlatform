package com.revplay.revplay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "play_history", indexes = {
        @Index(name = "idx_history_user_played", columnList = "user_id, played_at"),
        @Index(name = "idx_history_song", columnList = "song_id"),
        @Index(name = "idx_history_episode", columnList = "episode_id"),
        @Index(name = "idx_history_played_at", columnList = "played_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_id")
    private Long playId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "song_id")
    private Long songId;

    @Column(name = "episode_id")
    private Long episodeId;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @Column(name = "completed", nullable = false)
    private Boolean completed;

    @PrePersist
    protected void onCreate() {
        this.playedAt = LocalDateTime.now();
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
