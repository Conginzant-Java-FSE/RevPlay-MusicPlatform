package com.revplay.musicplatform.audit.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuditLogRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation")
    void validRequestPassesValidation() {
        AuditLogRequest request = new AuditLogRequest();
        request.setAction("CREATE");
        request.setPerformedBy(1L);
        request.setEntityType("SONG");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("missing required fields fail validation")
    void missingRequiredFieldsFailValidation() {
        AuditLogRequest request = new AuditLogRequest();

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
