package com.revplay.musicplatform.ads.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AdImpressionTest {

    @Test
    @DisplayName("AdImpression stores and returns fields correctly")
    void adImpressionStoresFields() {
        Long adId = 1L;
        Long userId = 2L;
        Long songId = 3L;
        LocalDateTime playedAt = LocalDateTime.now();

        AdImpression impression = new AdImpression();
        impression.setAdId(adId);
        impression.setUserId(userId);
        impression.setSongId(songId);
        impression.setPlayedAt(playedAt);

        assertThat(impression.getAdId()).isEqualTo(adId);
        assertThat(impression.getUserId()).isEqualTo(userId);
        assertThat(impression.getSongId()).isEqualTo(songId);
        assertThat(impression.getPlayedAt()).isEqualTo(playedAt);
    }
}
