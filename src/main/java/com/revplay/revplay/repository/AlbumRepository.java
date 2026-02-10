package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Album;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByArtist_ArtistId(Long artistId);
}

