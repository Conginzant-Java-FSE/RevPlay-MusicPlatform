package com.revplay.musicplatform.catalog.repository;



import com.revplay.musicplatform.catalog.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
    Page<Song> findByArtistId(Long artistId, Pageable pageable);
    long countByAlbumId(Long albumId);
    long countByArtistId(Long artistId);
    boolean existsByAlbumIdAndTitleIgnoreCaseAndIsActiveTrue(Long albumId, String title);
    boolean existsByAlbumIdAndTitleIgnoreCaseAndIsActiveTrueAndSongIdNot(Long albumId, String title, Long songId);
}

