package com.revplay.musicplatform.security.service.impl;

import com.revplay.musicplatform.common.TestDataFactory;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.JwtProperties;
import com.revplay.musicplatform.user.entity.User;
import com.revplay.musicplatform.user.enums.UserRole;
import com.revplay.musicplatform.user.exception.AuthUnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class JwtServiceImplTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha";
    private static final long ACCESS_EXPIRY_SECONDS = 3600L;
    private static final long REFRESH_EXPIRY_SECONDS = 1209600L;

    private JwtServiceImpl jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setAccessTokenExpirationSeconds(ACCESS_EXPIRY_SECONDS);
        properties.setRefreshTokenExpirationSeconds(REFRESH_EXPIRY_SECONDS);
        jwtService = new JwtServiceImpl(properties);
        user = TestDataFactory.buildUser(10L, "jwt@test.com", "jwtUser", UserRole.ARTIST);
    }

    @Test
    @DisplayName("generateAccessToken creates parseable access token with expected claims")
    void generateAccessToken_validClaims() {
        String token = jwtService.generateAccessToken(user);

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("10");
        assertThat(claims.get("username", String.class)).isEqualTo("jwtUser");
        assertThat(claims.get("role", String.class)).isEqualTo("ARTIST");
        assertThat(claims.get("token_type", String.class)).isEqualTo("access");
    }

    @Test
    @DisplayName("generateRefreshToken creates token_type refresh")
    void generateRefreshToken_hasRefreshType() {
        String token = jwtService.generateRefreshToken(user);

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.get("token_type", String.class)).isEqualTo("refresh");
    }

    @Test
    @DisplayName("isAccessToken returns true for access token")
    void isAccessToken_accessTokenTrue() {
        assertThat(jwtService.isAccessToken(jwtService.generateAccessToken(user))).isTrue();
    }

    @Test
    @DisplayName("isAccessToken returns false for refresh token")
    void isAccessToken_refreshTokenFalse() {
        assertThat(jwtService.isAccessToken(jwtService.generateRefreshToken(user))).isFalse();
    }

    @Test
    @DisplayName("isRefreshToken returns true for refresh token")
    void isRefreshToken_refreshTokenTrue() {
        assertThat(jwtService.isRefreshToken(jwtService.generateRefreshToken(user))).isTrue();
    }

    @Test
    @DisplayName("toPrincipal extracts userId username and role")
    void toPrincipal_valid() {
        String token = jwtService.generateAccessToken(user);

        AuthenticatedUserPrincipal principal = jwtService.toPrincipal(token);

        assertThat(principal.userId()).isEqualTo(10L);
        assertThat(principal.username()).isEqualTo("jwtUser");
        assertThat(principal.role()).isEqualTo(UserRole.ARTIST);
    }

    @Test
    @DisplayName("toPrincipal throws AuthUnauthorizedException for bad role claim")
    void toPrincipal_badRoleClaim() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("10")
                .claim("username", "jwtUser")
                .claim("role", "BAD_ROLE")
                .claim("token_type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ACCESS_EXPIRY_SECONDS)))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtService.toPrincipal(token))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Invalid token role claim");
    }

    @Test
    @DisplayName("parseToken throws AuthUnauthorizedException for tampered token")
    void parseToken_tamperedToken() {
        String token = jwtService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.parseToken(token + "tamper"))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Invalid or expired token");
    }

    @Test
    @DisplayName("parseToken throws AuthUnauthorizedException for expired token")
    void parseToken_expiredToken() {
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret(TEST_SECRET);
        expiredProperties.setAccessTokenExpirationSeconds(-1);
        expiredProperties.setRefreshTokenExpirationSeconds(REFRESH_EXPIRY_SECONDS);
        JwtServiceImpl expiredService = new JwtServiceImpl(expiredProperties);

        String expiredToken = expiredService.generateAccessToken(user);

        assertThatThrownBy(() -> jwtService.parseToken(expiredToken))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Invalid or expired token");
    }

    @Test
    @DisplayName("getExpiry returns instant aligned with token expiration")
    void getExpiry_matchesTokenExpiration() {
        String token = jwtService.generateAccessToken(user);

        Instant expiry = jwtService.getExpiry(token);

        assertThat(expiry).isAfter(Instant.now().minusSeconds(1));
        assertThat(expiry).isBefore(Instant.now().plusSeconds(ACCESS_EXPIRY_SECONDS + 5));
    }
}
