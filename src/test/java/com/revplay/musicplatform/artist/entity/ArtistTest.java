package com.revplay.musicplatform.artist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistTest {

    @Test
    @DisplayName("onCreate sets createdAt, updatedAt and default verified false")
    void onCreateSetsDefaults() {
        Artist artist = new Artist();
        artist.setVerified(null);

        artist.onCreate();

        assertThat(artist.getCreatedAt()).isNotNull();
        assertThat(artist.getUpdatedAt()).isNotNull();
        assertThat(artist.getVerified()).isFalse();
    }

    @Test
    @DisplayName("onCreate keeps explicit verified value")
    void onCreateKeepsExplicitVerified() {
        Artist artist = new Artist();
        artist.setVerified(true);

        artist.onCreate();

        assertThat(artist.getVerified()).isTrue();
    }

    @Test
    @DisplayName("onUpdate refreshes updatedAt timestamp")
    void onUpdateRefreshesTimestamp() {
        Artist artist = new Artist();
        artist.setUpdatedAt(LocalDateTime.now().minusHours(1));

        artist.onUpdate();

        assertThat(artist.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }
}
