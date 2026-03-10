package com.revplay.musicplatform.artist.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.catalog.enums.SocialPlatform;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistSocialLinkUpdateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        ArtistSocialLinkUpdateRequest request = new ArtistSocialLinkUpdateRequest();
        request.setPlatform(SocialPlatform.YOUTUBE);
        request.setUrl("https://youtube.com/a");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("blank url fails validation")
    void blankUrlFailsValidation() {
        ArtistSocialLinkUpdateRequest request = new ArtistSocialLinkUpdateRequest();
        request.setPlatform(SocialPlatform.YOUTUBE);
        request.setUrl(" ");

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
