package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UnauthorizedExceptionTest {

    private static final String MESSAGE = "unauthorized";

    @Test
    @DisplayName("constructor sets message")
    void constructorSetsMessage() {
        UnauthorizedException exception = new UnauthorizedException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }
}
