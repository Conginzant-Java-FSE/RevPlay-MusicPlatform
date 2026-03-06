package com.revplay.musicplatform.security.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Tag("unit")
class TokenRevocationServiceImplTest {

    private static final String TEST_TOKEN = "test-token";
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private TokenRevocationServiceImpl tokenRevocationService;

    @BeforeEach
    void setUp() {
        tokenRevocationService = new TokenRevocationServiceImpl();
    }

    @Test
    @DisplayName("revoke → isRevoked: returns true")
    void revoke_SetsTokenAsRevoked() {
        Instant expiry = Instant.now().plusSeconds(3600);
        tokenRevocationService.revoke(TEST_TOKEN, expiry);

        assertThat(tokenRevocationService.isRevoked(TEST_TOKEN)).isTrue();
    }

    @Test
    @DisplayName("isRevoked for unknown token: returns false")
    void isRevoked_UnknownToken_ReturnsFalse() {
        assertThat(tokenRevocationService.isRevoked("unknown")).isFalse();
    }

    @Test
    @DisplayName("revokeAllForUser: all tokens for that user return isRevoked=true")
    void revokeAllForUser_RevokesAllUserTokens() {
        String token1 = "token-1";
        String token2 = "token-2";
        Instant expiry = Instant.now().plusSeconds(3600);

        tokenRevocationService.registerIssuedToken(USER_ID, token1, expiry);
        tokenRevocationService.registerIssuedToken(USER_ID, token2, expiry);

        tokenRevocationService.revokeAllForUser(USER_ID);

        assertThat(tokenRevocationService.isRevoked(token1)).isTrue();
        assertThat(tokenRevocationService.isRevoked(token2)).isTrue();
    }

    @Test
    @DisplayName("revokeAllForUser: tokens of OTHER users unaffected")
    void revokeAllForUser_OtherUsersUnaffected() {
        String userToken = "user-token";
        String otherToken = "other-token";
        Instant expiry = Instant.now().plusSeconds(3600);

        tokenRevocationService.registerIssuedToken(USER_ID, userToken, expiry);
        tokenRevocationService.registerIssuedToken(OTHER_USER_ID, otherToken, expiry);

        tokenRevocationService.revokeAllForUser(USER_ID);

        assertThat(tokenRevocationService.isRevoked(userToken)).isTrue();
        assertThat(tokenRevocationService.isRevoked(otherToken)).isFalse();
    }

    @Test
    @DisplayName("revokeAllForUser with no tokens: no exception")
    void revokeAllForUser_NoTokens_NoException() {
        assertThatCode(() -> tokenRevocationService.revokeAllForUser(999L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("register then revokeAll then register new token: NOT revoked")
    void register_NewTokenAfterRevokeAll_NotRevoked() {
        String oldToken = "old-token";
        String newToken = "new-token";
        Instant expiry = Instant.now().plusSeconds(3600);

        tokenRevocationService.registerIssuedToken(USER_ID, oldToken, expiry);
        tokenRevocationService.revokeAllForUser(USER_ID);

        tokenRevocationService.registerIssuedToken(USER_ID, newToken, expiry);

        assertThat(tokenRevocationService.isRevoked(oldToken)).isTrue();
        assertThat(tokenRevocationService.isRevoked(newToken)).isFalse();
    }

    @Test
    @DisplayName("revoke with null expiry: no NPE")
    void revoke_NullExpiry_NoNpe() {
        assertThatCode(() -> tokenRevocationService.revoke(TEST_TOKEN, null))
                .doesNotThrowAnyException();
        assertThat(tokenRevocationService.isRevoked(TEST_TOKEN)).isFalse();
    }
}
