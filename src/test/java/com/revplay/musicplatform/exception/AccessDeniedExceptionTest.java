package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AccessDeniedExceptionTest {

    private static final String MESSAGE = "forbidden";

    @Test
    @DisplayName("constructor sets message")
    void constructorSetsMessage() {
        AccessDeniedException exception = new AccessDeniedException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }
}
