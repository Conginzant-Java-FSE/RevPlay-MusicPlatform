package com.revplay.musicplatform.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PasswordResetTokenTest {

    @Test
    @DisplayName("setters and getters store token fields")
    void settersAndGettersStoreFields() {
        PasswordResetToken token = new PasswordResetToken();
        User user = new User();
        user.setUserId(9L);
        Instant now = Instant.now();

        token.setId(1L);
        token.setToken("t");
        token.setExpiryDate(now);
        token.setCreatedAt(now);
        token.setUser(user);

        assertThat(token.getId()).isEqualTo(1L);
        assertThat(token.getToken()).isEqualTo("t");
        assertThat(token.getUser()).isSameAs(user);
    }
}
