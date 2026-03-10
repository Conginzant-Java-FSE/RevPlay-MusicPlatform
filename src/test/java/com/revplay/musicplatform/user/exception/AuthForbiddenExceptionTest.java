package com.revplay.musicplatform.user.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuthForbiddenExceptionTest {
    @Test
    @DisplayName("constructor sets message")
    void constructorSetsMessage() {
        assertThat(new AuthForbiddenException("f")).hasMessage("f");
    }
}
