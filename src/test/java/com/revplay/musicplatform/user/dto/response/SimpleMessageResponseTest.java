package com.revplay.musicplatform.user.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SimpleMessageResponseTest {
    @Test
    @DisplayName("record exposes message")
    void recordExposesMessage() {
        SimpleMessageResponse response = new SimpleMessageResponse("ok");
        assertThat(response.message()).isEqualTo("ok");
    }
}
