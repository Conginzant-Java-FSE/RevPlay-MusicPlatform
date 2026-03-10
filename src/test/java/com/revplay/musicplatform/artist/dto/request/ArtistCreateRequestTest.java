package com.revplay.musicplatform.artist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.artist.enums.ArtistType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistCreateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        ArtistCreateRequest request = new ArtistCreateRequest();
        request.setDisplayName("Artist");
        request.setBio("Bio");
        request.setBannerImageUrl("https://img");
        request.setArtistType(ArtistType.MUSIC);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("missing displayName and artistType fail validation")
    void requiredFieldsFailValidationWhenMissing() {
        ArtistCreateRequest request = new ArtistCreateRequest();

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
