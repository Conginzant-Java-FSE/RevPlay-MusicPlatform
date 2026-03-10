package com.revplay.musicplatform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag("unit")
class ExceptionClassesTest {

    private static final String MESSAGE = "message";
    private static final String RESOURCE_NAME = "Song";
    private static final Long RESOURCE_ID = 99L;

    @Test
    @DisplayName("base exception subclass preserves message")
    void baseExceptionSubclassPreservesMessage() {
        TestBaseException exception = new TestBaseException(MESSAGE);

        assertThat(exception).hasMessage(MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("singleMessageExceptions")
    @DisplayName("single message exceptions preserve provided message")
    void singleMessageExceptionsPreserveMessage(RuntimeException exception) {
        assertThat(exception).hasMessage(MESSAGE);
    }

    @Test
    @DisplayName("resource not found constructor with resource and id formats message")
    void resourceNotFoundFormatsMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException(RESOURCE_NAME, RESOURCE_ID);

        assertThat(exception).hasMessage("Song not found: 99");
    }

    @Test
    @DisplayName("duplicate resource exception has conflict response status")
    void duplicateResourceHasConflictResponseStatus() {
        ResponseStatus responseStatus = DuplicateResourceException.class.getAnnotation(ResponseStatus.class);

        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("access denied exception has forbidden response status")
    void accessDeniedHasForbiddenResponseStatus() {
        ResponseStatus responseStatus = AccessDeniedException.class.getAnnotation(ResponseStatus.class);

        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static RuntimeException[] singleMessageExceptions() {
        return new RuntimeException[]{
                new ResourceNotFoundException(MESSAGE),
                new BadRequestException(MESSAGE),
                new ConflictException(MESSAGE),
                new UnauthorizedException(MESSAGE),
                new DuplicateResourceException(MESSAGE),
                new AccessDeniedException(MESSAGE)
        };
    }

    private static final class TestBaseException extends BaseException {
        private TestBaseException(String message) {
            super(message);
        }
    }
}
