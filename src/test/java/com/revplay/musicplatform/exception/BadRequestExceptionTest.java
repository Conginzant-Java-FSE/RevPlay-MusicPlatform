package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class BadRequestExceptionTest {

    private static final String MESSAGE = "bad-request";

    @Test
    @DisplayName("constructor sets message")
    void constructorSetsMessage() {
        BadRequestException exception = new BadRequestException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }
}
