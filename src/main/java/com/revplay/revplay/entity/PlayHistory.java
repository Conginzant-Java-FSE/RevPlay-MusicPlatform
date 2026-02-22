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
        @Index(name = "idx_history_played_at", columnList = "played_at"),
        @Index(name = "idx_history_completion", columnList = "completed, played_at"),
        @Index(name = "idx_history_duration", columnList = "play_duration_seconds, completed")
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

    @Column(name = "play_duration_seconds", nullable = false)
    private Integer playDurationSeconds = 0;

    @Column(name = "completed", nullable = false)
    private Boolean completed;

    @PrePersist
    protected void onCreate() {
        this.playedAt = LocalDateTime.now();
        if (this.playDurationSeconds == null) {
            this.playDurationSeconds = 0;
        }
        validateContentReference();
    }

    @PreUpdate
    protected void onUpdate() {
        validateContentReference();
    }

    private void validateContentReference() {
        boolean hasSong = songId != null;
        boolean hasEpisode = episodeId != null;

        if (!hasSong && !hasEpisode) {
            throw new IllegalStateException(
                    "Play history must reference either a song or an episode");
        }

        if (hasSong && hasEpisode) {
            throw new IllegalStateException(
                    "Play history cannot reference both song and episode simultaneously");
        }
    }

    public String getContentType() {
        return songId != null ? "SONG" : "EPISODE";
    }

    public Long getContentId() {
        return songId != null ? songId : episodeId;
    }
}
