package com.revplay.musicplatform.user.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AdminUserDetailsResponseTest {
    @Test
    @DisplayName("record exposes all components")
    void recordExposesAllComponents() {
        Instant createdAt = Instant.now();
        AdminUserDetailsResponse response = new AdminUserDetailsResponse(1L, "user", "a@b.com", "LISTENER", "ACTIVE", createdAt);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("user");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}
