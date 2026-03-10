package com.revplay.musicplatform.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ApiResponseTest {

    private static final String SUCCESS_MESSAGE = "ok";
    private static final String ERROR_MESSAGE = "error";
    private static final String DATA_VALUE = "payload";
    private static final String FIELD_NAME = "email";
    private static final String FIELD_REASON = "invalid";

    @Test
    @DisplayName("success with data sets success message data and timestamp")
    void successWithDataBuildsExpectedResponse() {
        ApiResponse<String> response = ApiResponse.success(SUCCESS_MESSAGE, DATA_VALUE);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(SUCCESS_MESSAGE);
        assertThat(response.getData()).isEqualTo(DATA_VALUE);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    @DisplayName("success without data sets success true and null data")
    void successWithoutDataBuildsExpectedResponse() {
        ApiResponse<Void> response = ApiResponse.success(SUCCESS_MESSAGE);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(SUCCESS_MESSAGE);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("error sets success false and timestamp")
    void errorBuildsExpectedResponse() {
        ApiResponse<Void> response = ApiResponse.error(ERROR_MESSAGE);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(ERROR_MESSAGE);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("builder and accessors support all properties")
    void builderAndAccessorsWork() {
        LocalDateTime timestamp = LocalDateTime.now();
        List<FieldError> errors = List.of(new FieldError(FIELD_NAME, FIELD_REASON));

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .message(ERROR_MESSAGE)
                .data(DATA_VALUE)
                .errors(errors)
                .timestamp(timestamp)
                .build();

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(ERROR_MESSAGE);
        assertThat(response.getData()).isEqualTo(DATA_VALUE);
        assertThat(response.getErrors()).containsExactlyElementsOf(errors);
        assertThat(response.getTimestamp()).isEqualTo(timestamp);

        response.setSuccess(true);
        response.setMessage(SUCCESS_MESSAGE);
        response.setData(null);
        response.setErrors(null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(SUCCESS_MESSAGE);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNull();
    }
}
