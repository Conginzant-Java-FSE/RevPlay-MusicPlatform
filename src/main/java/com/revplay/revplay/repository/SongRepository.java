package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Album;
import com.revplay.revplay.entity.Song;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findAllByArtist_ArtistId(Long artistId);

    // Used in update + delete (ownership check)
    Optional<Song> findBySongIdAndArtist_ArtistId(Long songId, Long artistId);
    // Duplicate title prevention
    boolean existsByArtist_ArtistIdAndTitleIgnoreCase(Long artistId, String title);
}
