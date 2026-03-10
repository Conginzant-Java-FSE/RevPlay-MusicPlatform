package com.revplay.musicplatform.analytics.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserStatisticsTest {

    private static final Long STAT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long TOTAL_PLAYLISTS = 3L;
    private static final Long TOTAL_FAVORITES = 4L;
    private static final Long TOTAL_LISTENING_SECONDS = 5L;
    private static final Long TOTAL_SONGS_PLAYED = 6L;
    private static final Long VERSION = 7L;

    @Test
    @DisplayName("UserStatistics stores and returns entity fields")
    void userStatisticsStoresAndReturnsFields() {
        Instant lastUpdated = Instant.now();

        UserStatistics statistics = new UserStatistics();
        statistics.setStatId(STAT_ID);
        statistics.setUserId(USER_ID);
        statistics.setTotalPlaylists(TOTAL_PLAYLISTS);
        statistics.setTotalFavoriteSongs(TOTAL_FAVORITES);
        statistics.setTotalListeningTimeSeconds(TOTAL_LISTENING_SECONDS);
        statistics.setTotalSongsPlayed(TOTAL_SONGS_PLAYED);
        statistics.setLastUpdated(lastUpdated);
        statistics.setVersion(VERSION);

        assertThat(statistics.getStatId()).isEqualTo(STAT_ID);
        assertThat(statistics.getUserId()).isEqualTo(USER_ID);
        assertThat(statistics.getTotalPlaylists()).isEqualTo(TOTAL_PLAYLISTS);
        assertThat(statistics.getTotalFavoriteSongs()).isEqualTo(TOTAL_FAVORITES);
        assertThat(statistics.getTotalListeningTimeSeconds()).isEqualTo(TOTAL_LISTENING_SECONDS);
        assertThat(statistics.getTotalSongsPlayed()).isEqualTo(TOTAL_SONGS_PLAYED);
        assertThat(statistics.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(statistics.getVersion()).isEqualTo(VERSION);
    }
}
