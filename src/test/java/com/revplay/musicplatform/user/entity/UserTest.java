package com.revplay.musicplatform.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.user.enums.UserRole;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserTest {

    @Test
    @DisplayName("setters and getters store all fields")
    void settersAndGettersStoreFields() {
        User user = new User();
        Instant now = Instant.now();
        LocalDateTime otpExpiry = LocalDateTime.of(2026, 3, 9, 10, 0);

        user.setUserId(1L);
        user.setEmail("a@b.com");
        user.setUsername("user");
        user.setPasswordHash("hash");
        user.setRole(UserRole.LISTENER);
        user.setIsActive(Boolean.TRUE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setEmailVerified(Boolean.TRUE);
        user.setEmailOtp("123456");
        user.setOtpExpiryTime(otpExpiry);
        user.setVersion(2L);

        assertThat(user.getUserId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("a@b.com");
        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(user.getRole()).isEqualTo(UserRole.LISTENER);
        assertThat(user.getEmailOtp()).isEqualTo("123456");
        assertThat(user.getOtpExpiryTime()).isEqualTo(otpExpiry);
    }
}
