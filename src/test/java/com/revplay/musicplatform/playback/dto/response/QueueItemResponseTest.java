package com.revplay.musicplatform.playback.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class QueueItemResponseTest {

    @Test
    @DisplayName("record exposes queue item response components")
    void recordExposesComponents() {
        Instant createdAt = Instant.parse("2026-03-09T03:00:00Z");
        QueueItemResponse response = new QueueItemResponse(1L, 10L, 20L, 30L, 2, createdAt);

        assertThat(response.queueId()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.songId()).isEqualTo(20L);
        assertThat(response.episodeId()).isEqualTo(30L);
        assertThat(response.position()).isEqualTo(2);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}
