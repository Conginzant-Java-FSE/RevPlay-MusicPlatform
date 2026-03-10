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
class LikeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid like request passes validation")
    void validLikeRequestPassesValidation() {
        LikeRequest request = new LikeRequest();
        request.setLikeableId(5L);
        request.setLikeableType("SONG");

        Set<ConstraintViolation<LikeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("invalid likeable type fails validation")
    void invalidLikeableTypeFailsValidation() {
        LikeRequest request = new LikeRequest();
        request.setLikeableId(5L);
        request.setLikeableType("ALBUM");

        Set<ConstraintViolation<LikeRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("likeableType");
    }
}
