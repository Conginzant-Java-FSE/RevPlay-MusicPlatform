package com.revplay.musicplatform.security;

import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.enums.UserRole;
import com.revplay.musicplatform.user.exception.AuthUnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String VALID_TOKEN = "valid-token";

    @Mock
    private JwtService jwtService;
    @Mock
    private TokenRevocationService tokenRevocationService;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("authorization absent keeps SecurityContext empty")
    void headerAbsent() throws ServletException, IOException {
        execute(null);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, tokenRevocationService);
    }

    @Test
    @DisplayName("authorization empty string keeps SecurityContext empty")
    void emptyHeader() throws ServletException, IOException {
        execute("");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, tokenRevocationService);
    }

    @Test
    @DisplayName("authorization Bearer blank keeps SecurityContext empty")
    void bearerBlank() throws ServletException, IOException {
        execute("Bearer ");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenRevocationService).isRevoked("");
    }

    @Test
    @DisplayName("authorization with Basic scheme keeps SecurityContext empty")
    void basicScheme() throws ServletException, IOException {
        execute("Basic dXNlcjpwYXNz");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, tokenRevocationService);
    }

    @Test
    @DisplayName("authorization Bearer without space keeps SecurityContext empty")
    void bearerWithoutSpace() throws ServletException, IOException {
        execute("Bearer");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, tokenRevocationService);
    }

    @Test
    @DisplayName("valid non-revoked access token sets AuthenticatedUserPrincipal")
    void validAccessToken() throws ServletException, IOException {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(5L, "alice", UserRole.ARTIST);
        when(tokenRevocationService.isRevoked(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.toPrincipal(VALID_TOKEN)).thenReturn(principal);

        execute("Bearer " + VALID_TOKEN);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(principal);
    }

    @Test
    @DisplayName("revoked token keeps SecurityContext empty and continues chain")
    void revokedToken() throws ServletException, IOException {
        when(tokenRevocationService.isRevoked(VALID_TOKEN)).thenReturn(true);

        execute("Bearer " + VALID_TOKEN);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).isAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("refresh token used in filter keeps SecurityContext empty")
    void refreshTokenAsAccess() throws ServletException, IOException {
        when(tokenRevocationService.isRevoked(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isAccessToken(VALID_TOKEN)).thenReturn(false);

        execute("Bearer " + VALID_TOKEN);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).toPrincipal(VALID_TOKEN);
    }

    @Test
    @DisplayName("tampered token propagates AuthUnauthorizedException")
    void tamperedToken() {
        when(tokenRevocationService.isRevoked(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isAccessToken(VALID_TOKEN)).thenThrow(new AuthUnauthorizedException("Invalid or expired token"));

        Throwable thrown = catchThrowable(() -> execute("Bearer " + VALID_TOKEN));

        assertThat(thrown).isInstanceOf(AuthUnauthorizedException.class);
    }

    @Test
    @DisplayName("invalid role claim propagates AuthUnauthorizedException")
    void invalidRoleClaim() {
        when(tokenRevocationService.isRevoked(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.toPrincipal(VALID_TOKEN)).thenThrow(new AuthUnauthorizedException("Invalid token role claim"));

        Throwable thrown = catchThrowable(() -> execute("Bearer " + VALID_TOKEN));

        assertThat(thrown).isInstanceOf(AuthUnauthorizedException.class);
    }

    @Test
    @DisplayName("already-authenticated context is overwritten in current implementation")
    void alreadyAuthenticated_actualBehavior() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("existing", null));
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(9L, "bob", UserRole.ADMIN);

        when(tokenRevocationService.isRevoked(VALID_TOKEN)).thenReturn(false);
        when(jwtService.isAccessToken(VALID_TOKEN)).thenReturn(true);
        when(jwtService.toPrincipal(VALID_TOKEN)).thenReturn(principal);

        execute("Bearer " + VALID_TOKEN);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(principal);
        verify(jwtService).isAccessToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("lowercase bearer is treated as invalid scheme")
    void lowercaseBearer_actualBehavior() throws ServletException, IOException {
        execute("bearer " + VALID_TOKEN);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, tokenRevocationService);
    }

    private void execute(String authorizationHeader) throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, tokenRevocationService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        if (authorizationHeader != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }
}
