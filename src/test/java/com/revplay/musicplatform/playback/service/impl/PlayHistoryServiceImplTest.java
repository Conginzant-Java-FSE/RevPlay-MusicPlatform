package com.revplay.musicplatform.playback.service.impl;

import com.revplay.musicplatform.playback.dto.request.TrackPlayRequest;
import com.revplay.musicplatform.playback.dto.response.PlayHistoryResponse;
import com.revplay.musicplatform.playback.entity.PlayHistory;
import com.revplay.musicplatform.playback.exception.PlaybackNotFoundException;
import com.revplay.musicplatform.playback.exception.PlaybackValidationException;
import com.revplay.musicplatform.playback.mapper.PlayHistoryMapper;
import com.revplay.musicplatform.playback.repository.PlayHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PlayHistoryServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long SONG_ID = 101L;
    private static final Long EPISODE_ID = 202L;
    private static final String USER_SQL = "SELECT COUNT(1) FROM users WHERE user_id = ?";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PlayHistoryRepository playHistoryRepository;
    @Mock
    private PlayHistoryMapper playHistoryMapper;

    @InjectMocks
    private PlayHistoryServiceImpl service;

    @Test
    @DisplayName("trackPlay song with provided fields saves correctly")
    void trackPlay_songProvided_savesCorrectly() {
        Instant playedAt = Instant.now().minusSeconds(60);
        TrackPlayRequest request = new TrackPlayRequest(USER_ID, SONG_ID, null, true, 120, playedAt);
        mockUserExists();

        service.trackPlay(request);

        ArgumentCaptor<PlayHistory> captor = ArgumentCaptor.forClass(PlayHistory.class);
        verify(playHistoryRepository).save(captor.capture());
        PlayHistory saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getSongId()).isEqualTo(SONG_ID);
        assertThat(saved.getEpisodeId()).isNull();
        assertThat(saved.getCompleted()).isTrue();
        assertThat(saved.getPlayDurationSeconds()).isEqualTo(120);
        assertThat(saved.getPlayedAt()).isEqualTo(playedAt);
    }

    @Test
    @DisplayName("trackPlay episode sets episodeId and defaults fields")
    void trackPlay_episode_defaultsApplied() {
        TrackPlayRequest request = new TrackPlayRequest(USER_ID, null, EPISODE_ID, null, null, null);
        mockUserExists();

        service.trackPlay(request);

        ArgumentCaptor<PlayHistory> captor = ArgumentCaptor.forClass(PlayHistory.class);
        verify(playHistoryRepository).save(captor.capture());
        PlayHistory saved = captor.getValue();
        assertThat(saved.getSongId()).isNull();
        assertThat(saved.getEpisodeId()).isEqualTo(EPISODE_ID);
        assertThat(saved.getCompleted()).isFalse();
        assertThat(saved.getPlayDurationSeconds()).isZero();
        assertThat(saved.getPlayedAt()).isNotNull();
    }

    @Test
    @DisplayName("trackPlay null request throws PlaybackValidationException userId required")
    void trackPlay_nullRequest_throws() {
        assertThatThrownBy(() -> service.trackPlay(null))
                .isInstanceOf(PlaybackValidationException.class)
                .hasMessage("userId is required");
    }

    @Test
    @DisplayName("trackPlay neither song nor episode throws PlaybackValidationException")
    void trackPlay_noContent_throws() {
        TrackPlayRequest request = new TrackPlayRequest(USER_ID, null, null, false, 0, Instant.now());
        mockUserExists();

        assertThatThrownBy(() -> service.trackPlay(request))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("trackPlay both song and episode throws PlaybackValidationException")
    void trackPlay_bothContentIds_throws() {
        TrackPlayRequest request = new TrackPlayRequest(USER_ID, SONG_ID, EPISODE_ID, false, 0, Instant.now());
        mockUserExists();

        assertThatThrownBy(() -> service.trackPlay(request))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("trackPlay user missing throws PlaybackNotFoundException")
    void trackPlay_userMissing_throws() {
        TrackPlayRequest request = new TrackPlayRequest(USER_ID, SONG_ID, null, false, 0, Instant.now());
        when(jdbcTemplate.queryForObject(USER_SQL, Long.class, USER_ID)).thenReturn(0L);

        assertThatThrownBy(() -> service.trackPlay(request))
                .isInstanceOf(PlaybackNotFoundException.class);
    }

    @Test
    @DisplayName("getHistory user records returns ordered list")
    void getHistory_userHasRecords_returnsList() {
        PlayHistory entity = new PlayHistory();
        entity.setPlayId(1L);
        entity.setUserId(USER_ID);
        entity.setSongId(SONG_ID);
        entity.setPlayedAt(Instant.now());
        PlayHistoryResponse response = new PlayHistoryResponse(1L, USER_ID, SONG_ID, null, entity.getPlayedAt(), false, 10);
        mockUserExists();
        when(playHistoryRepository.findByUserIdOrderByPlayedAtDescPlayIdDesc(USER_ID)).thenReturn(List.of(entity));
        when(playHistoryMapper.toDto(entity)).thenReturn(response);

        List<PlayHistoryResponse> actual = service.getHistory(USER_ID);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).songId()).isEqualTo(SONG_ID);
    }

    @Test
    @DisplayName("getHistory missing user throws PlaybackNotFoundException")
    void getHistory_userMissing_throws() {
        when(jdbcTemplate.queryForObject(USER_SQL, Long.class, USER_ID)).thenReturn(0L);

        assertThatThrownBy(() -> service.getHistory(USER_ID))
                .isInstanceOf(PlaybackNotFoundException.class);
    }

    @Test
    @DisplayName("recentlyPlayed uses fixed limit fifty")
    void recentlyPlayed_usesLimit50() {
        PlayHistory entity = new PlayHistory();
        entity.setPlayId(1L);
        entity.setUserId(USER_ID);
        entity.setSongId(SONG_ID);
        entity.setPlayedAt(Instant.now());
        mockUserExists();
        when(playHistoryRepository.findByUserIdOrderByPlayedAtDescPlayIdDesc(USER_ID, PageRequest.of(0, 50)))
                .thenReturn(List.of(entity));
        when(playHistoryMapper.toDto(entity))
                .thenReturn(new PlayHistoryResponse(1L, USER_ID, SONG_ID, null, entity.getPlayedAt(), false, 0));

        List<PlayHistoryResponse> actual = service.recentlyPlayed(USER_ID);

        assertThat(actual).hasSizeLessThanOrEqualTo(50);
        verify(playHistoryRepository).findByUserIdOrderByPlayedAtDescPlayIdDesc(USER_ID, PageRequest.of(0, 50));
    }

    @Test
    @DisplayName("clearHistory user records deleted returns count")
    void clearHistory_deletes_returnsCount() {
        mockUserExists();
        when(playHistoryRepository.deleteByUserId(USER_ID)).thenReturn(3L);

        long deleted = service.clearHistory(USER_ID);

        assertThat(deleted).isEqualTo(3L);
    }

    @Test
    @DisplayName("clearHistory user missing throws PlaybackNotFoundException")
    void clearHistory_userMissing_throws() {
        when(jdbcTemplate.queryForObject(USER_SQL, Long.class, USER_ID)).thenReturn(0L);

        assertThatThrownBy(() -> service.clearHistory(USER_ID))
                .isInstanceOf(PlaybackNotFoundException.class);
    }

    private void mockUserExists() {
        when(jdbcTemplate.queryForObject(USER_SQL, Long.class, USER_ID)).thenReturn(1L);
    }
}
