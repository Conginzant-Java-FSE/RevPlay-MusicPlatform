package com.revplay.musicplatform.user.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ForgotPasswordRequestTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Test
    @DisplayName("invalid email fails validation")
    void invalidEmailFailsValidation() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("bad");
        assertThat(validator.validate(request)).isNotEmpty();
    }
}
