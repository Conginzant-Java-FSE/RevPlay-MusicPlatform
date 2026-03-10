package com.revplay.musicplatform.ads.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserAdPlaybackStateTest {

    @Test
    @DisplayName("UserAdPlaybackState stores and returns playback counters")
    void userAdPlaybackStateStoresFields() {
        Long userId = 10L;
        Integer songsPlayedCount = 7;
        Long lastServedAdId = 5L;

        UserAdPlaybackState state = new UserAdPlaybackState();
        state.setUserId(userId);
        state.setSongsPlayedCount(songsPlayedCount);
        state.setLastServedAdId(lastServedAdId);

        assertThat(state.getUserId()).isEqualTo(userId);
        assertThat(state.getSongsPlayedCount()).isEqualTo(songsPlayedCount);
        assertThat(state.getLastServedAdId()).isEqualTo(lastServedAdId);
    }
}
