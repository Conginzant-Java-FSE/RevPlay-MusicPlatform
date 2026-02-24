package com.revplay.revplay.service.impl;

import com.revplay.revplay.entity.UserStatistics;
import com.revplay.revplay.repository.UserStatisticsRepository;
import com.revplay.revplay.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(UserStatisticsServiceImpl.class);

    private final UserStatisticsRepository statsRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(Long userId) {
        return statsRepository.findByUserId(userId)
                .orElseGet(() -> calculateAndSaveStatistics(userId));
    }

    @Override
    @Transactional
    public UserStatistics calculateAndSaveStatistics(Long userId) {

        Integer totalPlaylists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM playlists WHERE user_id = ?",
                Integer.class,
                userId
        );

        Integer totalFavoriteSongs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_likes WHERE user_id = ?",
                Integer.class,
                userId
        );

        Long totalListeningTime = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(play_duration_seconds), 0) FROM play_history WHERE user_id = ?",
                Long.class,
                userId
        );

        Long totalSongsPlayed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM play_history WHERE user_id = ?",
                Long.class,
                userId
        );

        UserStatistics stats = statsRepository.findByUserId(userId)
                .orElse(new UserStatistics());

        stats.setUserId(userId);
        stats.setTotalPlaylists(totalPlaylists != null ? totalPlaylists : 0);
        stats.setTotalFavoriteSongs(totalFavoriteSongs != null ? totalFavoriteSongs : 0);
        stats.setTotalListeningTimeSeconds(totalListeningTime != null ? totalListeningTime : 0L);
        stats.setTotalSongsPlayed(totalSongsPlayed != null ? totalSongsPlayed : 0L);
        stats.setLastUpdated(LocalDateTime.now());

        UserStatistics saved = statsRepository.save(stats);

        logger.info("Updated statistics for userId={}", userId);

        return saved;
    }

    @Override
    @Transactional
    public void recalculateAllStatistics() {

        List<Long> allUserIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT user_id FROM (" +
                        " SELECT user_id FROM playlists " +
                        " UNION " +
                        " SELECT user_id FROM user_likes " +
                        " UNION " +
                        " SELECT user_id FROM play_history" +
                        ") AS all_users",
                Long.class
        );

        for (Long userId : allUserIds) {
            try {
                calculateAndSaveStatistics(userId);
            } catch (Exception e) {
                logger.error("Failed to update statistics for userId={}", userId, e);
            }
        }
    }
}
