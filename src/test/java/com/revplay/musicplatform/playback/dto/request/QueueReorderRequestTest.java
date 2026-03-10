package com.revplay.musicplatform.playback.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class QueueReorderRequestTest {

    private static final Long USER_ID = 44L;
    private static final List<Long> QUEUE_IDS = List.of(1L, 2L, 3L);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("valid request passes validation and exposes record values")
    void validRequestPassesValidation() {
        QueueReorderRequest request = new QueueReorderRequest(USER_ID, QUEUE_IDS);

        Set<ConstraintViolation<QueueReorderRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
        assertThat(request.userId()).isEqualTo(USER_ID);
        assertThat(request.queueIdsInOrder()).containsExactlyElementsOf(QUEUE_IDS);
    }

    @Test
    @DisplayName("null userId fails validation")
    void nullUserIdFailsValidation() {
        QueueReorderRequest request = new QueueReorderRequest(null, QUEUE_IDS);

        Set<ConstraintViolation<QueueReorderRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("userId");
    }

    @Test
    @DisplayName("empty queue list fails validation")
    void emptyQueueListFailsValidation() {
        QueueReorderRequest request = new QueueReorderRequest(USER_ID, List.of());

        Set<ConstraintViolation<QueueReorderRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getPropertyPath().toString())).contains("queueIdsInOrder");
    }
}
