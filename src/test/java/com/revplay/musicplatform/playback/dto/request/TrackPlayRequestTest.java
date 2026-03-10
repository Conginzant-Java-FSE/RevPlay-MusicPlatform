package com.revplay.musicplatform.playback.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class TrackPlayRequestTest {

    private static final Long USER_ID = 55L;
    private static final Long SONG_ID = 99L;
    private static final Instant PLAYED_AT = Instant.parse("2026-03-09T02:00:00Z");

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request with zero duration passes validation")
    void zeroDurationPassesValidation() {
        TrackPlayRequest request = new TrackPlayRequest(USER_ID, SONG_ID, null, Boolean.TRUE, 0, PLAYED_AT);

        Set<ConstraintViolation<TrackPlayRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.userId()).isEqualTo(USER_ID);
        assertThat(request.songId()).isEqualTo(SONG_ID);
        assertThat(request.playedAt()).isEqualTo(PLAYED_AT);
    }

    @Test
    @DisplayName("negative duration fails validation")
    void negativeDurationFailsValidation() {
        TrackPlayRequest request = new TrackPlayRequest(USER_ID, SONG_ID, null, Boolean.FALSE, -1, PLAYED_AT);

        Set<ConstraintViolation<TrackPlayRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("playDurationSeconds");
    }

    @Test
    @DisplayName("null userId fails validation")
    void nullUserIdFailsValidation() {
        TrackPlayRequest request = new TrackPlayRequest(null, SONG_ID, null, null, null, PLAYED_AT);

        Set<ConstraintViolation<TrackPlayRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("userId");
    }
}
