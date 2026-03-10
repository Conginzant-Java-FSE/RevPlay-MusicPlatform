package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class BaseExceptionTest {

    private static final String MESSAGE = "base";

    @Test
    @DisplayName("subclass constructor preserves message")
    void subclassConstructorPreservesMessage() {
        TestBaseException exception = new TestBaseException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }

    private static final class TestBaseException extends BaseException {
        private TestBaseException(String message) {
            super(message);
        }
    }
}
