package com.revplay.musicplatform.playback.service.impl;

import com.revplay.musicplatform.playback.dto.request.QueueAddRequest;
import com.revplay.musicplatform.playback.dto.request.QueueReorderRequest;
import com.revplay.musicplatform.playback.dto.response.QueueItemResponse;
import com.revplay.musicplatform.playback.entity.QueueItem;
import com.revplay.musicplatform.playback.exception.PlaybackNotFoundException;
import com.revplay.musicplatform.playback.exception.PlaybackValidationException;
import com.revplay.musicplatform.playback.mapper.QueueItemMapper;
import com.revplay.musicplatform.playback.repository.QueueItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class QueueServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long OTHER_USER_ID = 20L;
    private static final Long SONG_ID = 101L;
    private static final Long EPISODE_ID = 202L;
    private static final Long QUEUE_ID = 1L;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private QueueItemRepository queueItemRepository;
    @Mock
    private QueueItemMapper queueItemMapper;

    @InjectMocks
    private QueueServiceImpl service;

    @Test
    @DisplayName("addToQueue song on empty queue saves at position one")
    void addToQueue_song_emptyQueue_positionOne() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, SONG_ID, null);
        QueueItem saved = queueItem(QUEUE_ID, USER_ID, SONG_ID, null, 1);
        QueueItemResponse response = new QueueItemResponse(QUEUE_ID, USER_ID, SONG_ID, null, 1, saved.getCreatedAt());

        mockUserExists(USER_ID);
        when(queueItemRepository.findTopByUserIdOrderByPositionDesc(USER_ID)).thenReturn(Optional.empty());
        when(queueItemRepository.save(any(QueueItem.class))).thenReturn(saved);
        when(queueItemMapper.toDto(saved)).thenReturn(response);

        QueueItemResponse actual = service.addToQueue(request);

        assertThat(actual.position()).isEqualTo(1);
        ArgumentCaptor<QueueItem> captor = ArgumentCaptor.forClass(QueueItem.class);
        verify(queueItemRepository).save(captor.capture());
        assertThat(captor.getValue().getSongId()).isEqualTo(SONG_ID);
    }

    @Test
    @DisplayName("addToQueue episode appends after existing queue")
    void addToQueue_episode_existingQueue_positionPlusOne() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, null, EPISODE_ID);
        QueueItem last = queueItem(99L, USER_ID, SONG_ID, null, 5);
        QueueItem saved = queueItem(QUEUE_ID, USER_ID, null, EPISODE_ID, 6);

        mockUserExists(USER_ID);
        when(queueItemRepository.findTopByUserIdOrderByPositionDesc(USER_ID)).thenReturn(Optional.of(last));
        when(queueItemRepository.save(any(QueueItem.class))).thenReturn(saved);
        when(queueItemMapper.toDto(saved)).thenReturn(new QueueItemResponse(QUEUE_ID, USER_ID, null, EPISODE_ID, 6, saved.getCreatedAt()));

        QueueItemResponse actual = service.addToQueue(request);

        assertThat(actual.position()).isEqualTo(6);
    }

    @Test
    @DisplayName("addToQueue null request throws PlaybackValidationException")
    void addToQueue_nullRequest_throws() {
        assertThatThrownBy(() -> service.addToQueue(null))
                .isInstanceOf(PlaybackValidationException.class)
                .hasMessage("userId is required");
    }

    @Test
    @DisplayName("addToQueue both song and episode missing throws validation")
    void addToQueue_bothIdsMissing_throws() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, null, null);
        mockUserExists(USER_ID);

        assertThatThrownBy(() -> service.addToQueue(request))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("addToQueue both song and episode set throws validation")
    void addToQueue_bothIdsSet_throws() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, SONG_ID, EPISODE_ID);
        mockUserExists(USER_ID);

        assertThatThrownBy(() -> service.addToQueue(request))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("addToQueue user not found throws PlaybackNotFoundException")
    void addToQueue_userNotFound_throws() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, SONG_ID, null);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), eq(USER_ID))).thenReturn(0L);

        assertThatThrownBy(() -> service.addToQueue(request))
                .isInstanceOf(PlaybackNotFoundException.class);
    }

    @Test
    @DisplayName("addToQueue DataAccessException from JdbcTemplate is propagated")
    void addToQueue_dataAccessException_propagates() {
        QueueAddRequest request = new QueueAddRequest(USER_ID, SONG_ID, null);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), eq(USER_ID)))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> service.addToQueue(request))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    @DisplayName("getQueue returns ordered items when present")
    void getQueue_itemsPresent_returnsOrderedList() {
        QueueItem item1 = queueItem(1L, USER_ID, SONG_ID, null, 1);
        QueueItem item2 = queueItem(2L, USER_ID, null, EPISODE_ID, 2);
        mockUserExists(USER_ID);
        when(queueItemRepository.findByUserIdOrderByPositionAscQueueIdAsc(USER_ID)).thenReturn(List.of(item1, item2));
        when(queueItemMapper.toDto(item1)).thenReturn(new QueueItemResponse(1L, USER_ID, SONG_ID, null, 1, item1.getCreatedAt()));
        when(queueItemMapper.toDto(item2)).thenReturn(new QueueItemResponse(2L, USER_ID, null, EPISODE_ID, 2, item2.getCreatedAt()));

        List<QueueItemResponse> actual = service.getQueue(USER_ID);

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).position()).isEqualTo(1);
        assertThat(actual.get(1).position()).isEqualTo(2);
    }

    @Test
    @DisplayName("getQueue empty queue returns empty list")
    void getQueue_empty_returnsEmptyList() {
        mockUserExists(USER_ID);
        when(queueItemRepository.findByUserIdOrderByPositionAscQueueIdAsc(USER_ID)).thenReturn(List.of());

        List<QueueItemResponse> actual = service.getQueue(USER_ID);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("removeFromQueue found item deletes successfully")
    void removeFromQueue_found_deletes() {
        QueueItem item = queueItem(QUEUE_ID, USER_ID, SONG_ID, null, 1);
        when(queueItemRepository.findById(QUEUE_ID)).thenReturn(Optional.of(item));

        service.removeFromQueue(QUEUE_ID);

        verify(queueItemRepository).delete(item);
    }

    @Test
    @DisplayName("removeFromQueue missing item throws PlaybackNotFoundException")
    void removeFromQueue_notFound_throws() {
        when(queueItemRepository.findById(QUEUE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeFromQueue(QUEUE_ID))
                .isInstanceOf(PlaybackNotFoundException.class);
    }

    @Test
    @DisplayName("reorder valid request updates positions")
    void reorder_valid_updatesPositions() {
        QueueItem q1 = queueItem(11L, USER_ID, 111L, null, 1);
        QueueItem q2 = queueItem(12L, USER_ID, 112L, null, 2);
        QueueReorderRequest request = new QueueReorderRequest(USER_ID, List.of(12L, 11L));

        mockUserExists(USER_ID);
        when(queueItemRepository.findByUserIdForUpdate(USER_ID)).thenReturn(List.of(q1, q2));
        when(queueItemRepository.findByUserIdOrderByPositionAscQueueIdAsc(USER_ID)).thenReturn(List.of(q2, q1));
        when(queueItemMapper.toDto(q2)).thenReturn(new QueueItemResponse(12L, USER_ID, 112L, null, 1, q2.getCreatedAt()));
        when(queueItemMapper.toDto(q1)).thenReturn(new QueueItemResponse(11L, USER_ID, 111L, null, 2, q1.getCreatedAt()));

        List<QueueItemResponse> actual = service.reorder(request);

        assertThat(actual).hasSize(2);
        assertThat(q2.getPosition()).isEqualTo(1);
        assertThat(q1.getPosition()).isEqualTo(2);
        verify(queueItemRepository).saveAll(List.of(q2, q1));
    }

    @Test
    @DisplayName("reorder queue item from different user throws PlaybackValidationException")
    void reorder_itemNotOwned_throws() {
        QueueItem q1 = queueItem(11L, USER_ID, 111L, null, 1);
        QueueReorderRequest request = new QueueReorderRequest(USER_ID, List.of(99L));
        mockUserExists(USER_ID);
        when(queueItemRepository.findByUserIdForUpdate(USER_ID)).thenReturn(List.of(q1));

        assertThatThrownBy(() -> service.reorder(request))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("reorder duplicate queue ids throws PlaybackValidationException")
    void reorder_duplicateQueueIds_throws() {
        QueueItem q1 = queueItem(11L, USER_ID, 111L, null, 1);
        QueueReorderRequest request = new QueueReorderRequest(USER_ID, List.of(11L, 11L));
        mockUserExists(USER_ID);
        when(queueItemRepository.findByUserIdForUpdate(USER_ID)).thenReturn(List.of(q1));

        assertThatThrownBy(() -> service.reorder(request))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("reorder empty list throws PlaybackValidationException")
    void reorder_emptyList_throws() {
        QueueReorderRequest request = new QueueReorderRequest(USER_ID, List.of());

        assertThatThrownBy(() -> service.reorder(request))
                .isInstanceOf(PlaybackValidationException.class);
    }

    private void mockUserExists(Long userId) {
        when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), eq(userId))).thenReturn(1L);
    }

    private QueueItem queueItem(Long queueId, Long userId, Long songId, Long episodeId, Integer position) {
        QueueItem item = new QueueItem();
        item.setQueueId(queueId);
        item.setUserId(userId);
        item.setSongId(songId);
        item.setEpisodeId(episodeId);
        item.setPosition(position);
        item.setCreatedAt(Instant.now());
        return item;
    }
}
