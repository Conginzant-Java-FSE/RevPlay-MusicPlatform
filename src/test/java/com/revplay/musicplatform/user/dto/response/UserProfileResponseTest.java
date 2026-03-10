package com.revplay.musicplatform.user.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserProfileResponseTest {
    @Test
    @DisplayName("record exposes profile response components")
    void recordExposesComponents() {
        UserProfileResponse response = new UserProfileResponse(1L, "Full", "bio", "/img", "IN");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.country()).isEqualTo("IN");
    }
}
