package com.revplay.musicplatform.artist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.catalog.enums.SocialPlatform;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistSocialLinkCreateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        ArtistSocialLinkCreateRequest request = new ArtistSocialLinkCreateRequest();
        request.setPlatform(SocialPlatform.INSTAGRAM);
        request.setUrl("https://instagram.com/a");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("missing platform and blank url fail validation")
    void requiredFieldsFailValidationWhenMissing() {
        ArtistSocialLinkCreateRequest request = new ArtistSocialLinkCreateRequest();
        request.setUrl(" ");

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
