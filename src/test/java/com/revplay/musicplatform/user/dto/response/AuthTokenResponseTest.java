package com.revplay.musicplatform.user.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuthTokenResponseTest {
    @Test
    @DisplayName("record exposes all token response components")
    void recordExposesAllComponents() {
        UserResponse user = new UserResponse(1L, "a@b.com", "user", "LISTENER", true, null, null);
        AuthTokenResponse response = new AuthTokenResponse("Bearer", "access", 3600L, "refresh", 1209600L, user);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user()).isSameAs(user);
    }
}
