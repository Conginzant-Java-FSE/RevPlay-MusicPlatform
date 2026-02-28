package com.revplay.musicplatform.user.dto.response;

import java.time.Instant;

public record ForgotPasswordResponse(
        String message,
        String resetToken,
        Instant expiresAt
) {
}
