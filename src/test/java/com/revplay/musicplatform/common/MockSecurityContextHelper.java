package com.revplay.musicplatform.common;

import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MockSecurityContextHelper {

    public void setMockUser(Long userId, String username, String roleName) {
        mockUser(userId, username, UserRole.from(roleName));
    }

    public static void mockUser(Long userId, String username, UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, username, role);
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name()));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
