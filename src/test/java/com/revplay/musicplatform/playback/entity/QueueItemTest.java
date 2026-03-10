package com.revplay.musicplatform.playback.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class QueueItemTest {

    private static final Long QUEUE_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long SONG_ID = 20L;
    private static final Long EPISODE_ID = 30L;
    private static final Integer POSITION = 2;
    private static final Long VERSION = 3L;

    @Test
    @DisplayName("setters and getters store queue item fields")
    void settersAndGettersStoreFields() {
        QueueItem queueItem = new QueueItem();
        Instant createdAt = Instant.parse("2026-03-09T00:00:00Z");

        queueItem.setQueueId(QUEUE_ID);
        queueItem.setUserId(USER_ID);
        queueItem.setSongId(SONG_ID);
        queueItem.setEpisodeId(EPISODE_ID);
        queueItem.setPosition(POSITION);
        queueItem.setCreatedAt(createdAt);
        queueItem.setVersion(VERSION);

        assertThat(queueItem.getQueueId()).isEqualTo(QUEUE_ID);
        assertThat(queueItem.getUserId()).isEqualTo(USER_ID);
        assertThat(queueItem.getSongId()).isEqualTo(SONG_ID);
        assertThat(queueItem.getEpisodeId()).isEqualTo(EPISODE_ID);
        assertThat(queueItem.getPosition()).isEqualTo(POSITION);
        assertThat(queueItem.getCreatedAt()).isEqualTo(createdAt);
        assertThat(queueItem.getVersion()).isEqualTo(VERSION);
    }
}
