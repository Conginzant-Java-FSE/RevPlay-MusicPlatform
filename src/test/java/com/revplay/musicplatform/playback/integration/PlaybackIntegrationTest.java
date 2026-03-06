package com.revplay.musicplatform.playback.integration;

import com.revplay.musicplatform.playback.dto.request.QueueAddRequest;
import com.revplay.musicplatform.playback.dto.request.QueueReorderRequest;
import com.revplay.musicplatform.playback.dto.request.TrackPlayRequest;
import com.revplay.musicplatform.playback.dto.response.PlayHistoryResponse;
import com.revplay.musicplatform.playback.dto.response.QueueItemResponse;
import com.revplay.musicplatform.playback.repository.PlayHistoryRepository;
import com.revplay.musicplatform.playback.repository.QueueItemRepository;
import com.revplay.musicplatform.playback.service.PlayHistoryService;
import com.revplay.musicplatform.playback.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class PlaybackIntegrationTest {

    private static final Long USER_ID = 100L;
    private static final Long SONG_A = 101L;
    private static final Long SONG_B = 102L;
    private static final Long SONG_C = 103L;

    private final PlayHistoryService playHistoryService;
    private final QueueService queueService;
    private final PlayHistoryRepository playHistoryRepository;
    private final QueueItemRepository queueItemRepository;
    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;

    @Autowired
    PlaybackIntegrationTest(
            PlayHistoryService playHistoryService,
            QueueService queueService,
            PlayHistoryRepository playHistoryRepository,
            QueueItemRepository queueItemRepository,
            JdbcTemplate jdbcTemplate,
            CacheManager cacheManager) {
        this.playHistoryService = playHistoryService;
        this.queueService = queueService;
        this.playHistoryRepository = playHistoryRepository;
        this.queueItemRepository = queueItemRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.cacheManager = cacheManager;
    }

    @BeforeEach
    void setUp() {
        queueItemRepository.deleteAll();
        playHistoryRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM users");
        insertUser(USER_ID);
        clearCache("analytics.trending");
        clearCache("analytics.dashboard");
    }

    @Test
    @DisplayName("record five plays and getHistory returns five most recent first")
    void recordFivePlays_historyOrdered() {
        for (int i = 0; i < 5; i++) {
            playHistoryService.trackPlay(new TrackPlayRequest(
                    USER_ID,
                    SONG_A + i,
                    null,
                    false,
                    30 + i,
                    Instant.now().minusSeconds(i)
            ));
        }

        List<PlayHistoryResponse> history = playHistoryService.getHistory(USER_ID);

        assertThat(history).hasSize(5);
        assertThat(history.get(0).playedAt()).isAfterOrEqualTo(history.get(1).playedAt());
        assertThat(history.get(1).playedAt()).isAfterOrEqualTo(history.get(2).playedAt());
    }

    @Test
    @DisplayName("trackPlay with completed true persists completed flag")
    void trackPlay_completedTrue_persists() {
        playHistoryService.trackPlay(new TrackPlayRequest(
                USER_ID,
                SONG_A,
                null,
                true,
                99,
                Instant.now()
        ));

        List<PlayHistoryResponse> history = playHistoryService.getHistory(USER_ID);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).completed()).isTrue();
    }

    @Test
    @DisplayName("add three queue items reorder request succeeds and queue remains consistent")
    void queueAddReorder_verifyOrder() {
        QueueItemResponse first = queueService.addToQueue(new QueueAddRequest(USER_ID, SONG_A, null));
        QueueItemResponse second = queueService.addToQueue(new QueueAddRequest(USER_ID, SONG_B, null));
        QueueItemResponse third = queueService.addToQueue(new QueueAddRequest(USER_ID, SONG_C, null));

        queueService.reorder(new QueueReorderRequest(USER_ID, List.of(first.queueId(), second.queueId(), third.queueId())));
        List<QueueItemResponse> queue = queueService.getQueue(USER_ID);

        assertThat(queue).hasSize(3);
        assertThat(queue.get(0).songId()).isEqualTo(SONG_A);
        assertThat(queue.get(1).songId()).isEqualTo(SONG_B);
        assertThat(queue.get(2).songId()).isEqualTo(SONG_C);
    }

    @Test
    @DisplayName("remove all queue items then getQueue is empty")
    void clearQueueByRemovingAll_getQueueEmpty() {
        queueService.addToQueue(new QueueAddRequest(USER_ID, SONG_A, null));
        queueService.addToQueue(new QueueAddRequest(USER_ID, SONG_B, null));

        List<QueueItemResponse> queue = queueService.getQueue(USER_ID);
        queue.forEach(item -> queueService.removeFromQueue(item.queueId()));

        List<QueueItemResponse> after = queueService.getQueue(USER_ID);
        assertThat(after).isEmpty();
    }

    @Test
    @DisplayName("trackPlay evicts analytics caches")
    void trackPlay_evictsAnalyticsCaches() {
        Cache dashboard = cacheManager.getCache("analytics.dashboard");
        Cache trending = cacheManager.getCache("analytics.trending");
        assertThat(dashboard).isNotNull();
        assertThat(trending).isNotNull();
        dashboard.put("metrics", "cached");
        trending.put("song:DAILY:10", "cached");

        playHistoryService.trackPlay(new TrackPlayRequest(USER_ID, SONG_A, null, false, 1, Instant.now()));

        assertThat(dashboard.get("metrics")).isNull();
        assertThat(trending.get("song:DAILY:10")).isNull();
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    private void insertUser(Long userId) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                "INSERT INTO users (user_id, email, username, password_hash, role, is_active, created_at, updated_at, email_verified, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId,
                "user" + userId + "@mail.test",
                "user" + userId,
                "hash",
                "LISTENER",
                true,
                now,
                now,
                true,
                0L
        );
    }
}
