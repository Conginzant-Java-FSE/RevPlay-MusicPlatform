package com.revplay.revplay.repository;

import com.revplay.revplay.entity.PlayHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    Page<PlayHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT ph FROM PlayHistory ph WHERE ph.userId = :userId ORDER BY ph.playedAt DESC")
    List<PlayHistory> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    long deleteByUserId(Long userId);

    @Query("SELECT COUNT(ph) FROM PlayHistory ph WHERE ph.playedAt >= :startDate")
    long countPlaysInPeriod(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT ph.songId, COUNT(ph) FROM PlayHistory ph " +
            "WHERE ph.userId = :userId AND ph.songId IS NOT NULL " +
            "AND ph.playedAt >= :startDate " +
            "GROUP BY ph.songId ORDER BY COUNT(ph) DESC")
    List<Object[]> findTopSongsByUserId(@Param("userId") Long userId,
                                        @Param("startDate") LocalDateTime startDate,
                                        Pageable pageable);

    @Query("SELECT ph.episodeId, COUNT(ph) FROM PlayHistory ph " +
            "WHERE ph.userId = :userId AND ph.episodeId IS NOT NULL " +
            "AND ph.playedAt >= :startDate " +
            "GROUP BY ph.episodeId ORDER BY COUNT(ph) DESC")
    List<Object[]> findTopEpisodesByUserId(@Param("userId") Long userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           Pageable pageable);
}