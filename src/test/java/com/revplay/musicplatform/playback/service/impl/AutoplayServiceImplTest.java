package com.revplay.musicplatform.playback.service.impl;

import com.revplay.musicplatform.analytics.dto.response.SongRecommendationResponse;
import com.revplay.musicplatform.analytics.service.RecommendationService;
import com.revplay.musicplatform.catalog.entity.Song;
import com.revplay.musicplatform.catalog.repository.SongRepository;
import com.revplay.musicplatform.playback.dto.response.QueueItemResponse;
import com.revplay.musicplatform.playback.exception.PlaybackNotFoundException;
import com.revplay.musicplatform.playback.exception.PlaybackValidationException;
import com.revplay.musicplatform.playback.service.QueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AutoplayServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long CURRENT_SONG_ID = 100L;
    private static final Long NEXT_SONG_ID = 200L;
    private static final Long QUEUE_ID = 1L;

    @Mock
    private QueueService queueService;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private AutoplayServiceImpl service;

    @Test
    @DisplayName("null userId or currentSongId throws PlaybackValidationException")
    void getNextSong_nullInputs_throws() {
        assertThatThrownBy(() -> service.getNextSong(null, CURRENT_SONG_ID))
                .isInstanceOf(PlaybackValidationException.class);
        assertThatThrownBy(() -> service.getNextSong(USER_ID, null))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("empty queue falls back to recommendations")
    void getNextSong_emptyQueue_fallsBackToRecommendations() {
        Song recommended = new Song();
        recommended.setSongId(NEXT_SONG_ID);
        when(queueService.getQueue(USER_ID)).thenReturn(List.of());
        when(recommendationService.similarSongs(CURRENT_SONG_ID, 1))
                .thenReturn(List.of(new SongRecommendationResponse(NEXT_SONG_ID, "n", 1L, "a", 10L)));
        when(songRepository.findById(NEXT_SONG_ID)).thenReturn(Optional.of(recommended));

        Song actual = service.getNextSong(USER_ID, CURRENT_SONG_ID);

        assertThat(actual.getSongId()).isEqualTo(NEXT_SONG_ID);
        verify(recommendationService).similarSongs(CURRENT_SONG_ID, 1);
    }

    @Test
    @DisplayName("queue contains current song and next song returns next queue song")
    void getNextSong_queueHasCurrentAndNextSong_returnsQueueSong() {
        QueueItemResponse current = new QueueItemResponse(QUEUE_ID, USER_ID, CURRENT_SONG_ID, null, 1, Instant.now());
        QueueItemResponse next = new QueueItemResponse(QUEUE_ID + 1, USER_ID, NEXT_SONG_ID, null, 2, Instant.now());
        Song song = new Song();
        song.setSongId(NEXT_SONG_ID);
        when(queueService.getQueue(USER_ID)).thenReturn(List.of(current, next));
        when(queueService.next(USER_ID, QUEUE_ID)).thenReturn(next);
        when(songRepository.findById(NEXT_SONG_ID)).thenReturn(Optional.of(song));

        Song actual = service.getNextSong(USER_ID, CURRENT_SONG_ID);

        assertThat(actual.getSongId()).isEqualTo(NEXT_SONG_ID);
    }

    @Test
    @DisplayName("current song not in queue falls back to recommendations")
    void getNextSong_currentNotInQueue_fallback() {
        Song recommended = new Song();
        recommended.setSongId(NEXT_SONG_ID);
        when(queueService.getQueue(USER_ID))
                .thenReturn(List.of(new QueueItemResponse(1L, USER_ID, 999L, null, 1, Instant.now())));
        when(recommendationService.similarSongs(CURRENT_SONG_ID, 1))
                .thenReturn(List.of(new SongRecommendationResponse(NEXT_SONG_ID, "n", 1L, "a", 10L)));
        when(songRepository.findById(NEXT_SONG_ID)).thenReturn(Optional.of(recommended));

        Song actual = service.getNextSong(USER_ID, CURRENT_SONG_ID);

        assertThat(actual.getSongId()).isEqualTo(NEXT_SONG_ID);
    }

    @Test
    @DisplayName("next queue item episode falls back to recommendations")
    void getNextSong_nextQueueEpisode_fallback() {
        QueueItemResponse current = new QueueItemResponse(QUEUE_ID, USER_ID, CURRENT_SONG_ID, null, 1, Instant.now());
        QueueItemResponse nextEpisode = new QueueItemResponse(QUEUE_ID + 1, USER_ID, null, 500L, 2, Instant.now());
        Song recommended = new Song();
        recommended.setSongId(NEXT_SONG_ID);
        when(queueService.getQueue(USER_ID)).thenReturn(List.of(current, nextEpisode));
        when(queueService.next(USER_ID, QUEUE_ID)).thenReturn(nextEpisode);
        when(recommendationService.similarSongs(CURRENT_SONG_ID, 1))
                .thenReturn(List.of(new SongRecommendationResponse(NEXT_SONG_ID, "n", 1L, "a", 10L)));
        when(songRepository.findById(NEXT_SONG_ID)).thenReturn(Optional.of(recommended));

        Song actual = service.getNextSong(USER_ID, CURRENT_SONG_ID);

        assertThat(actual.getSongId()).isEqualTo(NEXT_SONG_ID);
    }

    @Test
    @DisplayName("next queue song missing in repository throws PlaybackNotFoundException")
    void getNextSong_nextQueueSongNotFound_throws() {
        QueueItemResponse current = new QueueItemResponse(QUEUE_ID, USER_ID, CURRENT_SONG_ID, null, 1, Instant.now());
        QueueItemResponse next = new QueueItemResponse(QUEUE_ID + 1, USER_ID, NEXT_SONG_ID, null, 2, Instant.now());
        when(queueService.getQueue(USER_ID)).thenReturn(List.of(current, next));
        when(queueService.next(USER_ID, QUEUE_ID)).thenReturn(next);
        when(songRepository.findById(NEXT_SONG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getNextSong(USER_ID, CURRENT_SONG_ID))
                .isInstanceOf(PlaybackNotFoundException.class);
    }

    @Test
    @DisplayName("empty recommendations throws PlaybackNotFoundException no autoplay song")
    void getNextSong_emptyRecommendations_throws() {
        when(queueService.getQueue(USER_ID)).thenReturn(List.of());
        when(recommendationService.similarSongs(CURRENT_SONG_ID, 1)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getNextSong(USER_ID, CURRENT_SONG_ID))
                .isInstanceOf(PlaybackNotFoundException.class)
                .hasMessage("No autoplay song available");
    }

    @Test
    @DisplayName("recommended song id missing throws PlaybackNotFoundException")
    void getNextSong_recommendedSongMissing_throws() {
        when(queueService.getQueue(USER_ID)).thenReturn(List.of());
        when(recommendationService.similarSongs(CURRENT_SONG_ID, 1))
                .thenReturn(List.of(new SongRecommendationResponse(NEXT_SONG_ID, "n", 1L, "a", 10L)));
        when(songRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getNextSong(USER_ID, CURRENT_SONG_ID))
                .isInstanceOf(PlaybackNotFoundException.class);
    }
}
