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
class UpdatePlaylistRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request fields pass validation")
    void validRequestPassesValidation() {
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setName("Updated");
        request.setDescription("Desc");
        request.setIsPublic(Boolean.FALSE);

        Set<ConstraintViolation<UpdatePlaylistRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.getIsPublic()).isFalse();
    }

    @Test
    @DisplayName("name longer than max length fails validation")
    void nameTooLongFailsValidation() {
        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setName("x".repeat(101));

        Set<ConstraintViolation<UpdatePlaylistRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("name");
    }
}
