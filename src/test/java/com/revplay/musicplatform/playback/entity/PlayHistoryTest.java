package com.revplay.musicplatform.playback.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlayHistoryTest {

    private static final Long PLAY_ID = 1L;
    private static final Long USER_ID = 11L;
    private static final Long SONG_ID = 21L;
    private static final Long EPISODE_ID = 31L;
    private static final Boolean COMPLETED = true;
    private static final Integer PLAY_DURATION_SECONDS = 145;
    private static final Long VERSION = 4L;

    @Test
    @DisplayName("setters and getters store play history fields")
    void settersAndGettersStoreFields() {
        PlayHistory playHistory = new PlayHistory();
        Instant playedAt = Instant.parse("2026-03-09T01:00:00Z");

        playHistory.setPlayId(PLAY_ID);
        playHistory.setUserId(USER_ID);
        playHistory.setSongId(SONG_ID);
        playHistory.setEpisodeId(EPISODE_ID);
        playHistory.setPlayedAt(playedAt);
        playHistory.setCompleted(COMPLETED);
        playHistory.setPlayDurationSeconds(PLAY_DURATION_SECONDS);
        playHistory.setVersion(VERSION);

        assertThat(playHistory.getPlayId()).isEqualTo(PLAY_ID);
        assertThat(playHistory.getUserId()).isEqualTo(USER_ID);
        assertThat(playHistory.getSongId()).isEqualTo(SONG_ID);
        assertThat(playHistory.getEpisodeId()).isEqualTo(EPISODE_ID);
        assertThat(playHistory.getPlayedAt()).isEqualTo(playedAt);
        assertThat(playHistory.getCompleted()).isEqualTo(COMPLETED);
        assertThat(playHistory.getPlayDurationSeconds()).isEqualTo(PLAY_DURATION_SECONDS);
        assertThat(playHistory.getVersion()).isEqualTo(VERSION);
    }
}
