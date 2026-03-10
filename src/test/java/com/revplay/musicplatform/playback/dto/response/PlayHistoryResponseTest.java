package com.revplay.musicplatform.playback.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlayHistoryResponseTest {

    @Test
    @DisplayName("record exposes play history response components")
    void recordExposesComponents() {
        Instant playedAt = Instant.parse("2026-03-09T04:00:00Z");
        PlayHistoryResponse response = new PlayHistoryResponse(1L, 11L, 21L, 31L, playedAt, Boolean.TRUE, 180);

        assertThat(response.playId()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(11L);
        assertThat(response.songId()).isEqualTo(21L);
        assertThat(response.episodeId()).isEqualTo(31L);
        assertThat(response.playedAt()).isEqualTo(playedAt);
        assertThat(response.completed()).isTrue();
        assertThat(response.playDurationSeconds()).isEqualTo(180);
    }
}
