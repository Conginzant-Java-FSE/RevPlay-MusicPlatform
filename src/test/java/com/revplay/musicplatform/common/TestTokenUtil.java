package com.revplay.musicplatform.common;

import com.revplay.musicplatform.security.JwtProperties;
import com.revplay.musicplatform.security.service.impl.JwtServiceImpl;
import com.revplay.musicplatform.user.entity.User;
import com.revplay.musicplatform.user.enums.UserRole;

public final class TestTokenUtil {

    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha";
    private static final long ACCESS_TOKEN_EXPIRY = 3600;
    private static final long REFRESH_TOKEN_EXPIRY = 1209600;
    private static final JwtServiceImpl JWT_SERVICE;

    static {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setAccessTokenExpirationSeconds(ACCESS_TOKEN_EXPIRY);
        properties.setRefreshTokenExpirationSeconds(REFRESH_TOKEN_EXPIRY);
        JWT_SERVICE = new JwtServiceImpl(properties);
    }

    private TestTokenUtil() {
    }

    public static String generateAccessToken(Long userId, String username, UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setRole(role);
        return JWT_SERVICE.generateAccessToken(user);
    }

    public static String generateRefreshToken(Long userId, String username, UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setRole(role);
        return JWT_SERVICE.generateRefreshToken(user);
    }

    public static String bearerHeader(String token) {
        return "Bearer " + token;
    }
}
