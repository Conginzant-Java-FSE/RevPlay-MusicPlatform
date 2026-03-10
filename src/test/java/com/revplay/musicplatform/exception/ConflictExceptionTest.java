package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ConflictExceptionTest {

    private static final String MESSAGE = "conflict";

    @Test
    @DisplayName("constructor sets message")
    void constructorSetsMessage() {
        ConflictException exception = new ConflictException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }
}
