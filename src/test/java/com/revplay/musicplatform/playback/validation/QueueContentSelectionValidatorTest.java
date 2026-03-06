package com.revplay.musicplatform.playback.validation;

import com.revplay.musicplatform.playback.dto.request.QueueAddRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class QueueContentSelectionValidatorTest {

    private final QueueContentSelectionValidator validator = new QueueContentSelectionValidator();

    @Test
    @DisplayName("isValid both song and episode null returns false")
    void isValid_bothNull_false() {
        boolean valid = validator.isValid(new QueueAddRequest(1L, null, null), null);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("isValid both song and episode set returns false")
    void isValid_bothSet_false() {
        boolean valid = validator.isValid(new QueueAddRequest(1L, 10L, 20L), null);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("isValid only song set returns true")
    void isValid_onlySong_true() {
        boolean valid = validator.isValid(new QueueAddRequest(1L, 10L, null), null);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isValid only episode set returns true")
    void isValid_onlyEpisode_true() {
        boolean valid = validator.isValid(new QueueAddRequest(1L, null, 20L), null);
        assertThat(valid).isTrue();
    }
}
