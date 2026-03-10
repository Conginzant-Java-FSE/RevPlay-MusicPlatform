package com.revplay.musicplatform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuthenticatedUserPrincipalTest {

    @Test
    @DisplayName("record stores user id username and role")
    void recordFields() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", UserRole.ADMIN);

        assertThat(principal.userId()).isEqualTo(1L);
        assertThat(principal.username()).isEqualTo("user");
        assertThat(principal.role()).isEqualTo(UserRole.ADMIN);
    }
}
