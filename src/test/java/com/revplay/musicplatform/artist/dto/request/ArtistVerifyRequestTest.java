package com.revplay.musicplatform.artist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistVerifyRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("null verified fails validation")
    void nullVerifiedFailsValidation() {
        ArtistVerifyRequest request = new ArtistVerifyRequest();

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("non null verified passes validation")
    void verifiedValuePassesValidation() {
        ArtistVerifyRequest request = new ArtistVerifyRequest();
        request.setVerified(Boolean.TRUE);

        assertThat(validator.validate(request)).isEmpty();
    }
}
