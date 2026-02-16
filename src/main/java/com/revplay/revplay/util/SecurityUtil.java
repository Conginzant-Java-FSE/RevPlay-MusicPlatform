package com.revplay.revplay.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static String getCurrentUserEmail() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }

        // JwtAuthenticationFilter sets principal as UserDetails
        // auth.getName() becomes username/email
        return auth.getName();
    }
}
