package com.revplay.musicplatform.playback.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class FavoritedUserResponseTest {

    @Test
    @DisplayName("record exposes favorited user response components")
    void recordExposesComponents() {
        Instant favoritedAt = Instant.parse("2026-03-09T05:00:00Z");
        FavoritedUserResponse response = new FavoritedUserResponse(9L, "user9", "u9@mail.com", "User Nine", favoritedAt);

        assertThat(response.userId()).isEqualTo(9L);
        assertThat(response.username()).isEqualTo("user9");
        assertThat(response.email()).isEqualTo("u9@mail.com");
        assertThat(response.fullName()).isEqualTo("User Nine");
        assertThat(response.favoritedAt()).isEqualTo(favoritedAt);
    }
}
