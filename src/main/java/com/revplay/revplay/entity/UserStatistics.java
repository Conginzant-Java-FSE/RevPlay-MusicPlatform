package com.revplay.revplay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_playlists")
    private Integer totalPlaylists = 0;

    @Column(name = "total_favorite_songs")
    private Integer totalFavoriteSongs = 0;

    @Column(name = "total_listening_time_seconds")
    private Long totalListeningTimeSeconds = 0L;

    @Column(name = "total_songs_played")
    private Long totalSongsPlayed = 0L;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}