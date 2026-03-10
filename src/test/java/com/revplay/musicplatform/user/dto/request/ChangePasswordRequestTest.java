package com.revplay.musicplatform.user.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ChangePasswordRequestTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest("current123", "newpassword");
        assertThat(validator.validate(request)).isEmpty();
    }
}
