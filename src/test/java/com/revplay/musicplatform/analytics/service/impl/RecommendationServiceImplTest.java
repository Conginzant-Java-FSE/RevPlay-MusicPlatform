package com.revplay.musicplatform.analytics.service.impl;

import com.revplay.musicplatform.analytics.dto.response.ForYouRecommendationsResponse;
import com.revplay.musicplatform.analytics.dto.response.SongRecommendationResponse;
import com.revplay.musicplatform.playback.exception.PlaybackValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long SONG_ID = 100L;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RecommendationServiceImpl service;

    @Test
    @DisplayName("forUser with history returns recommendations from both sections")
    void forUser_withHistory_returnsRecommendations() {
        SongRecommendationResponse fromGenre = new SongRecommendationResponse(1L, "G1", 9L, "A", 50L);
        SongRecommendationResponse fromSimilar = new SongRecommendationResponse(2L, "S1", 8L, "B", 40L);

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(fromGenre));
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyLong(), anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(fromSimilar));

        ForYouRecommendationsResponse response = service.forUser(USER_ID, 10);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.youMightLike()).hasSize(1);
        assertThat(response.popularWithSimilarUsers()).hasSize(1);
    }

    @Test
    @DisplayName("forUser database errors return empty recommendation lists")
    void forUser_dataAccessError_returnsEmpty() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyLong(), anyLong(), anyInt()))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        ForYouRecommendationsResponse response = service.forUser(USER_ID, 10);

        assertThat(response.youMightLike()).isEmpty();
        assertThat(response.popularWithSimilarUsers()).isEmpty();
    }

    @Test
    @DisplayName("forUser invalid limit throws PlaybackValidationException")
    void forUser_invalidLimit_throws() {
        assertThatThrownBy(() -> service.forUser(USER_ID, 0))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("similarSongs returns list when query succeeds")
    void similarSongs_found_returnsList() {
        SongRecommendationResponse rec = new SongRecommendationResponse(5L, "X", 1L, "Y", 99L);
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyLong(), anyInt()))
                .thenReturn(List.of(rec));

        List<SongRecommendationResponse> response = service.similarSongs(SONG_ID, 5);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).songId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("similarSongs no result returns empty list")
    void similarSongs_empty_returnsEmpty() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyLong(), anyInt()))
                .thenReturn(List.of());

        List<SongRecommendationResponse> response = service.similarSongs(SONG_ID, 5);

        assertThat(response).isEmpty();
    }
}
