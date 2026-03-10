package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ResourceNotFoundExceptionTest {

    private static final String MESSAGE = "not-found";

    @Test
    @DisplayName("constructor with message sets message")
    void constructorWithMessageSetsMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }

    @Test
    @DisplayName("constructor with resource and id formats message")
    void constructorWithResourceAndIdFormatsMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Song", 12L);

        assertThat(exception).hasMessage("Song not found: 12");
    }
}
