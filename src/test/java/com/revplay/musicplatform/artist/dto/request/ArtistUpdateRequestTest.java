package com.revplay.musicplatform.artist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.artist.enums.ArtistType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistUpdateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        ArtistUpdateRequest request = new ArtistUpdateRequest();
        request.setDisplayName("Artist");
        request.setArtistType(ArtistType.PODCAST);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("missing required fields fail validation")
    void missingRequiredFieldsFailValidation() {
        ArtistUpdateRequest request = new ArtistUpdateRequest();

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
