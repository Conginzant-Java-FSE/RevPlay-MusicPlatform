package com.revplay.musicplatform.playlist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SongPositionRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        SongPositionRequest request = new SongPositionRequest();
        request.setSongId(10L);
        request.setPosition(2);

        Set<ConstraintViolation<SongPositionRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("missing fields fail validation")
    void missingFieldsFailValidation() {
        SongPositionRequest request = new SongPositionRequest();

        Set<ConstraintViolation<SongPositionRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString()))
                .contains("songId", "position");
    }
}
