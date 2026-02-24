package com.revplay.revplay.repository;

import com.revplay.revplay.entity.PlayHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    Page<PlayHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT ph FROM PlayHistory ph WHERE ph.userId = :userId ORDER BY ph.playedAt DESC")
    List<PlayHistory> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

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

    @Query("SELECT ph.songId, COUNT(ph) FROM PlayHistory ph " +
            "WHERE ph.songId IS NOT NULL AND ph.playedAt >= :startDate " +
            "GROUP BY ph.songId ORDER BY COUNT(ph) DESC")
    List<Object[]> findTrendingSongs(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT ph.episodeId, COUNT(ph) FROM PlayHistory ph " +
            "WHERE ph.episodeId IS NOT NULL AND ph.playedAt >= :startDate " +
            "GROUP BY ph.episodeId ORDER BY COUNT(ph) DESC")
    List<Object[]> findTrendingEpisodes(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT COUNT(ph), SUM(CASE WHEN ph.completed = true THEN 1 ELSE 0 END) " +
            "FROM PlayHistory ph WHERE ph.songId = :songId AND ph.playedAt >= :startDate")
    Object[] findSongCompletionMetrics(@Param("songId") Long songId,
                                       @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(ph), SUM(CASE WHEN ph.completed = true THEN 1 ELSE 0 END) " +
            "FROM PlayHistory ph WHERE ph.episodeId = :episodeId AND ph.playedAt >= :startDate")
    Object[] findEpisodeCompletionMetrics(@Param("episodeId") Long episodeId,
                                          @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.userId = :userId AND ph.playedAt >= :startDate")
    Double findUserCompletionRate(@Param("userId") Long userId,
                                  @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.playedAt >= :startDate")
    Double findPlatformCompletionRate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(ph.playDurationSeconds) FROM PlayHistory ph " +
            "WHERE ph.songId IS NOT NULL AND ph.playedAt >= :startDate")
    Double findAvgSongDuration(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(ph.playDurationSeconds) FROM PlayHistory ph " +
            "WHERE ph.episodeId IS NOT NULL AND ph.playedAt >= :startDate")
    Double findAvgEpisodeDuration(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(ph.playDurationSeconds) FROM PlayHistory ph " +
            "WHERE ph.userId = :userId AND ph.playedAt >= :startDate")
    Long findUserTotalListenTime(@Param("userId") Long userId,
                                 @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT ph.userId) FROM PlayHistory ph " +
            "WHERE ph.songId = :songId AND ph.playedAt >= :startDate")
    Long countUniqueSongListeners(@Param("songId") Long songId,
                                  @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT ph.userId) FROM PlayHistory ph " +
            "WHERE ph.episodeId = :episodeId AND ph.playedAt >= :startDate")
    Long countUniqueEpisodeListeners(@Param("episodeId") Long episodeId,
                                     @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT ph.userId) FROM PlayHistory ph " +
            "WHERE ph.playedAt >= :startDate")
    Long countActiveUsers(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT HOUR(ph.playedAt), COUNT(ph) FROM PlayHistory ph " +
            "WHERE ph.userId = :userId AND ph.playedAt >= :startDate " +
            "GROUP BY HOUR(ph.playedAt) ORDER BY HOUR(ph.playedAt)")
    List<Object[]> findListeningHoursByUserId(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate);

    @Query("SELECT HOUR(ph.playedAt), COUNT(ph) FROM PlayHistory ph " +
            "WHERE ph.playedAt >= :startDate " +
            "GROUP BY HOUR(ph.playedAt) ORDER BY HOUR(ph.playedAt)")
    List<Object[]> findPlatformPeakHours(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT ph2.songId, COUNT(DISTINCT ph2.userId) FROM PlayHistory ph1 " +
            "JOIN PlayHistory ph2 ON ph1.userId = ph2.userId " +
            "WHERE ph1.songId = :songId " +
            "AND ph2.songId != :songId " +
            "AND ph2.songId IS NOT NULL " +
            "AND ph2.completed = true " +
            "AND ph1.playedAt >= :startDate " +
            "GROUP BY ph2.songId ORDER BY COUNT(DISTINCT ph2.userId) DESC")
    List<Object[]> findSongCoListening(@Param("songId") Long songId,
                                       @Param("startDate") LocalDateTime startDate,
                                       Pageable pageable);

    @Query("SELECT ph2.episodeId, COUNT(DISTINCT ph2.userId) FROM PlayHistory ph1 " +
            "JOIN PlayHistory ph2 ON ph1.userId = ph2.userId " +
            "WHERE ph1.episodeId = :episodeId " +
            "AND ph2.episodeId != :episodeId " +
            "AND ph2.episodeId IS NOT NULL " +
            "AND ph2.completed = true " +
            "AND ph1.playedAt >= :startDate " +
            "GROUP BY ph2.episodeId ORDER BY COUNT(DISTINCT ph2.userId) DESC")
    List<Object[]> findEpisodeCoListening(@Param("episodeId") Long episodeId,
                                          @Param("startDate") LocalDateTime startDate,
                                          Pageable pageable);

    @Query("SELECT ph FROM PlayHistory ph WHERE ph.userId = :userId ORDER BY ph.playedAt DESC")
    List<PlayHistory> findUserRecentSession(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ph.songId, " +
            "AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.songId IS NOT NULL " +
            "AND ph.playedAt >= :startDate " +
            "GROUP BY ph.songId " +
            "HAVING AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 >= :minCompletionRate " +
            "ORDER BY AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 DESC")
    List<Object[]> findHighCompletionSongs(@Param("startDate") LocalDateTime startDate,
                                           @Param("minCompletionRate") double minCompletionRate,
                                           Pageable pageable);

    @Query("SELECT COUNT(DISTINCT ph.songId) FROM PlayHistory ph " +
            "WHERE ph.userId = :userId AND ph.songId IS NOT NULL " +
            "AND ph.playedAt >= :startDate")
    Long countUniqueSongsInPeriod(@Param("userId") Long userId,
                                  @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT ph.episodeId) FROM PlayHistory ph " +
            "WHERE ph.userId = :userId AND ph.episodeId IS NOT NULL " +
            "AND ph.playedAt >= :startDate")
    Long countUniqueEpisodesInPeriod(@Param("userId") Long userId,
                                     @Param("startDate") LocalDateTime startDate);

    @Query("SELECT CASE WHEN ph.songId IS NOT NULL THEN 'SONG' ELSE 'EPISODE' END, " +
            "COUNT(ph), COUNT(DISTINCT ph.userId), AVG(ph.playDurationSeconds), " +
            "AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.playedAt >= :startDate " +
            "GROUP BY CASE WHEN ph.songId IS NOT NULL THEN 'SONG' ELSE 'EPISODE' END")
    List<Object[]> findPlatformContentComparison(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT CAST(ph.playedAt AS date), COUNT(ph), COUNT(DISTINCT ph.userId), " +
            "AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.playedAt >= :startDate " +
            "GROUP BY CAST(ph.playedAt AS date) ORDER BY CAST(ph.playedAt AS date)")
    List<Object[]> findDailyEngagementTrends(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('YEARWEEK', ph.playedAt), COUNT(ph), COUNT(DISTINCT ph.userId), " +
            "AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.playedAt >= :startDate " +
            "GROUP BY FUNCTION('YEARWEEK', ph.playedAt) ORDER BY FUNCTION('YEARWEEK', ph.playedAt)")
    List<Object[]> findWeeklyEngagementTrends(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', ph.playedAt, '%Y-%m'), COUNT(ph), COUNT(DISTINCT ph.userId), " +
            "AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.playedAt >= :startDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', ph.playedAt, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', ph.playedAt, '%Y-%m')")
    List<Object[]> findMonthlyEngagementTrends(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT CASE WHEN ph.songId IS NOT NULL THEN 'SONG' ELSE 'EPISODE' END, " +
            "COUNT(ph), COUNT(DISTINCT COALESCE(ph.songId, ph.episodeId)), " +
            "AVG(ph.playDurationSeconds), " +
            "AVG(CASE WHEN ph.completed = true THEN 1.0 ELSE 0.0 END) * 100 " +
            "FROM PlayHistory ph WHERE ph.userId = :userId AND ph.playedAt >= :startDate " +
            "GROUP BY CASE WHEN ph.songId IS NOT NULL THEN 'SONG' ELSE 'EPISODE' END")
    List<Object[]> findContentComparisonByUser(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM PlayHistory ph WHERE ph.userId = :userId")
    long deleteByUserId(@Param("userId") Long userId);
}