package com.revplay.revplay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "song_genres",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_song_genre",
                columnNames = {"song_id", "genre_id"}
        )
)
public class SongGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_genre_id")
    private Long songGenreId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "genre_id", nullable = false)
    private Long genreId;
}
