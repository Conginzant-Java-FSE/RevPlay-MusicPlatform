package com.revplay.musicplatform.analytics.service.impl;

import com.revplay.musicplatform.analytics.dto.response.UserStatisticsResponse;
import com.revplay.musicplatform.analytics.entity.UserStatistics;
import com.revplay.musicplatform.analytics.mapper.UserStatisticsMapper;
import com.revplay.musicplatform.analytics.repository.UserStatisticsRepository;
import com.revplay.musicplatform.playback.exception.PlaybackNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserStatisticsServiceImplTest {

    private static final Long USER_ID = 80L;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private UserStatisticsRepository repository;
    @Mock
    private UserStatisticsMapper userStatisticsMapper;

    @InjectMocks
    private UserStatisticsServiceImpl service;

    @Test
    @DisplayName("getByUserId first call creates snapshot when repository is empty")
    void getByUserId_firstCall_createsSnapshot() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty(), Optional.of(stats(3L, 4L, 120L, 6L)));
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM users WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(1L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM playlists WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(3L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM user_likes WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(4L);
        when(jdbcTemplate.queryForObject(eq("SELECT COALESCE(SUM(play_duration_seconds), 0) FROM play_history WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(120L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM play_history WHERE user_id = ? AND song_id IS NOT NULL"), eq(Long.class), eq(USER_ID))).thenReturn(6L);
        when(userStatisticsMapper.toDto(any(UserStatistics.class))).thenReturn(dto(3L, 4L, 120L, 6L));

        UserStatisticsResponse response = service.getByUserId(USER_ID);

        assertThat(response.totalPlaylists()).isEqualTo(3L);
        assertThat(response.totalListeningTimeSeconds()).isEqualTo(120L);
        verify(jdbcTemplate).update(contains("INSERT INTO user_statistics"), eq(USER_ID), eq(3L), eq(4L), eq(120L), eq(6L));
    }

    @Test
    @DisplayName("getByUserId with existing history returns mapped values without refresh")
    void getByUserId_existingStats_noRefresh() {
        UserStatistics existing = stats(1L, 2L, 30L, 5L);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(userStatisticsMapper.toDto(existing)).thenReturn(dto(1L, 2L, 30L, 5L));

        UserStatisticsResponse response = service.getByUserId(USER_ID);

        assertThat(response.totalSongsPlayed()).isEqualTo(5L);
        assertThat(response.totalListeningTimeSeconds()).isEqualTo(30L);
    }

    @Test
    @DisplayName("refreshAndGet with no listening history returns zeroed stats")
    void refreshAndGet_noHistory_zeroed() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(stats(0L, 0L, 0L, 0L)));
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM users WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(1L);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM playlists WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(null);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM user_likes WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(null);
        when(jdbcTemplate.queryForObject(eq("SELECT COALESCE(SUM(play_duration_seconds), 0) FROM play_history WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(null);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM play_history WHERE user_id = ? AND song_id IS NOT NULL"), eq(Long.class), eq(USER_ID))).thenReturn(null);
        when(userStatisticsMapper.toDto(any(UserStatistics.class))).thenReturn(dto(0L, 0L, 0L, 0L));

        UserStatisticsResponse response = service.refreshAndGet(USER_ID);

        assertThat(response.totalPlaylists()).isZero();
        assertThat(response.totalListeningTimeSeconds()).isZero();
    }

    @Test
    @DisplayName("refreshAndGet for missing user throws PlaybackNotFoundException")
    void refreshAndGet_missingUser_throws() {
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(1) FROM users WHERE user_id = ?"), eq(Long.class), eq(USER_ID))).thenReturn(0L);

        assertThatThrownBy(() -> service.refreshAndGet(USER_ID))
                .isInstanceOf(PlaybackNotFoundException.class)
                .hasMessageContaining("does not exist");
    }

    private UserStatistics stats(Long playlists, Long favorites, Long listeningTime, Long played) {
        UserStatistics stats = new UserStatistics();
        stats.setUserId(USER_ID);
        stats.setTotalPlaylists(playlists);
        stats.setTotalFavoriteSongs(favorites);
        stats.setTotalListeningTimeSeconds(listeningTime);
        stats.setTotalSongsPlayed(played);
        stats.setLastUpdated(Instant.now());
        return stats;
    }

    private UserStatisticsResponse dto(Long playlists, Long favorites, Long listeningTime, Long played) {
        return new UserStatisticsResponse(USER_ID, playlists, favorites, listeningTime, played, Instant.now());
    }
}
