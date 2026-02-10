package com.revplay.revplay.repository;

import com.revplay.revplay.entity.SongGenre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongGenreRepository extends JpaRepository<SongGenre, Long> {
    List<SongGenre> findBySong_SongId(Long songId);
    void deleteBySong_SongId(Long songId);
    boolean existsBySong_SongIdAndGenreId(Long songId, Long genreId);
}
