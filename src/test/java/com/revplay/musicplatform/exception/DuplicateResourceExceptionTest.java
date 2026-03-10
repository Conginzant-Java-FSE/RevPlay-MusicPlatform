package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class DuplicateResourceExceptionTest {

    private static final String MESSAGE = "duplicate";

    @Test
    @DisplayName("constructor sets message")
    void constructorSetsMessage() {
        DuplicateResourceException exception = new DuplicateResourceException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }
}
