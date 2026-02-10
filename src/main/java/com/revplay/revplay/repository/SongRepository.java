package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Song;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByArtist_ArtistId(Long artistId);
    long countByAlbum_AlbumId(Long albumId);
}
