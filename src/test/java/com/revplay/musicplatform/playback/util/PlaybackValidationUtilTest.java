package com.revplay.musicplatform.playback.util;

import com.revplay.musicplatform.playback.exception.PlaybackValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class PlaybackValidationUtilTest {

    @Test
    @DisplayName("requireExactlyOneContentId both null throws PlaybackValidationException")
    void requireExactlyOneContentId_bothNull_throws() {
        assertThatThrownBy(() -> PlaybackValidationUtil.requireExactlyOneContentId(null, null))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("requireExactlyOneContentId both set throws PlaybackValidationException")
    void requireExactlyOneContentId_bothSet_throws() {
        assertThatThrownBy(() -> PlaybackValidationUtil.requireExactlyOneContentId(1L, 2L))
                .isInstanceOf(PlaybackValidationException.class);
    }

    @Test
    @DisplayName("requireExactlyOneContentId only song no exception")
    void requireExactlyOneContentId_onlySong_ok() {
        assertThatCode(() -> PlaybackValidationUtil.requireExactlyOneContentId(1L, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireExactlyOneContentId only episode no exception")
    void requireExactlyOneContentId_onlyEpisode_ok() {
        assertThatCode(() -> PlaybackValidationUtil.requireExactlyOneContentId(null, 2L))
                .doesNotThrowAnyException();
    }
}
