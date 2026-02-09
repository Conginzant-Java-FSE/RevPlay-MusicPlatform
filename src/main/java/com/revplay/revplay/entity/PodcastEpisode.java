package com.revplay.revplay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "podcast_episodes")
public class PodcastEpisode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "episode_id")
    private Long episodeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "podcast_id", nullable = false)
    private Podcast podcast;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "audio_url", nullable = false, length = 800)
    private String audioUrl;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "release_date")
    private LocalDate releaseDate;
}

