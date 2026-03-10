package com.revplay.musicplatform.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class FieldErrorTest {

    private static final String FIELD = "username";
    private static final String REASON = "required";
    private static final String UPDATED_FIELD = "email";
    private static final String UPDATED_REASON = "invalid";

    @Test
    @DisplayName("builder creates field error with expected values")
    void builderCreatesExpectedObject() {
        FieldError fieldError = FieldError.builder()
                .field(FIELD)
                .reason(REASON)
                .build();

        assertThat(fieldError.getField()).isEqualTo(FIELD);
        assertThat(fieldError.getReason()).isEqualTo(REASON);
    }

    @Test
    @DisplayName("setters and equals hashCode behave consistently")
    void settersAndEqualityWork() {
        FieldError fieldError = new FieldError();
        fieldError.setField(FIELD);
        fieldError.setReason(REASON);

        FieldError same = new FieldError(FIELD, REASON);
        FieldError different = new FieldError(UPDATED_FIELD, UPDATED_REASON);

        assertThat(fieldError).isEqualTo(same);
        assertThat(fieldError).hasSameHashCodeAs(same);
        assertThat(fieldError).isNotEqualTo(different);

        fieldError.setField(UPDATED_FIELD);
        fieldError.setReason(UPDATED_REASON);

        assertThat(fieldError.getField()).isEqualTo(UPDATED_FIELD);
        assertThat(fieldError.getReason()).isEqualTo(UPDATED_REASON);
    }
}
