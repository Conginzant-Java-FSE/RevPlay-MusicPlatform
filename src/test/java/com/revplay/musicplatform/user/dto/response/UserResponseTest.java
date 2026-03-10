package com.revplay.musicplatform.user.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserResponseTest {
    @Test
    @DisplayName("record exposes user response components")
    void recordExposesComponents() {
        Instant now = Instant.now();
        UserResponse response = new UserResponse(1L, "a@b.com", "user", "LISTENER", true, now, now);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.isActive()).isTrue();
    }
}
