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
class CreatePlaylistRequestTest {

    private static final String VALID_NAME = "My Playlist";
    private static final String VALID_DESCRIPTION = "Description";

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("default visibility is true")
    void defaultVisibilityIsTrue() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();

        assertThat(request.getIsPublic()).isTrue();
    }

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName(VALID_NAME);
        request.setDescription(VALID_DESCRIPTION);

        Set<ConstraintViolation<CreatePlaylistRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("blank name fails validation")
    void blankNameFailsValidation() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName(" ");

        Set<ConstraintViolation<CreatePlaylistRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("name");
    }
}
