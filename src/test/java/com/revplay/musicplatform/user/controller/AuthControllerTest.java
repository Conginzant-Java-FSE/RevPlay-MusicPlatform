package com.revplay.musicplatform.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.dto.request.*;
import com.revplay.musicplatform.user.dto.response.AuthTokenResponse;
import com.revplay.musicplatform.user.dto.response.SimpleMessageResponse;
import com.revplay.musicplatform.user.dto.response.UserResponse;
import com.revplay.musicplatform.user.enums.UserRole;
import com.revplay.musicplatform.user.exception.AuthConflictException;
import com.revplay.musicplatform.user.exception.AuthNotFoundException;
import com.revplay.musicplatform.user.exception.AuthUnauthorizedException;
import com.revplay.musicplatform.user.exception.AuthValidationException;
import com.revplay.musicplatform.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String REFRESH_URL = "/api/v1/auth/refresh";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";
    private static final String FORGOT_URL = "/api/v1/auth/forgot-password";
    private static final String RESET_URL = "/api/v1/auth/reset-password";
    private static final String CHANGE_URL = "/api/v1/auth/change-password";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    AuthControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST register valid payload returns 201 with token data")
    void register_valid() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(tokenResponse());

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("a@b.com", "user123", "Strong123!", "User Name", "LISTENER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST register missing email returns 400 with validation errors")
    void register_missingEmail() throws Exception {
        String body = "{\"username\":\"user123\",\"password\":\"Strong123!\",\"fullName\":\"Name\"}";

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST register conflict maps to 409")
    void register_conflict() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new AuthConflictException("Email already exists"));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("a@b.com", "user123", "Strong123!", "User Name", "LISTENER"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST login valid payload returns 200 with tokens")
    void login_valid() throws Exception {
        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(tokenResponse());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user123", "Strong123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST login unauthorized maps to 401")
    void login_unauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class), anyString())).thenThrow(new AuthUnauthorizedException("Invalid credentials"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user123", "bad"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST login with missing fields returns 400")
    void login_missingFields() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST refresh valid payload returns 200")
    void refresh_valid() throws Exception {
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(tokenResponse());

        mockMvc.perform(post(REFRESH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("valid-refresh"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST refresh invalid token returns 401")
    void refresh_invalidToken() throws Exception {
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenThrow(new AuthUnauthorizedException("Invalid refresh token"));

        mockMvc.perform(post(REFRESH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("bad"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST logout with Authorization header returns 200")
    void logout_withHeader() throws Exception {
        when(authService.logout(anyString())).thenReturn(new SimpleMessageResponse("Logged out successfully"));
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", UserRole.LISTENER);

        mockMvc.perform(post(LOGOUT_URL)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_LISTENER")))))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST logout without header still returns 200")
    void logout_withoutHeader() throws Exception {
        when(authService.logout(any())).thenReturn(new SimpleMessageResponse("Logged out successfully"));
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", UserRole.LISTENER);

        mockMvc.perform(post(LOGOUT_URL)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_LISTENER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST forgot-password valid payload returns 200")
    void forgotPassword_valid() throws Exception {
        when(authService.forgotPassword(any(ForgotPasswordRequest.class), anyString())).thenReturn(new SimpleMessageResponse("Password reset email sent successfully"));

        mockMvc.perform(post(FORGOT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("a@b.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST forgot-password unknown email maps to 404")
    void forgotPassword_notFound() throws Exception {
        when(authService.forgotPassword(any(ForgotPasswordRequest.class), anyString())).thenThrow(new AuthNotFoundException("User not found"));

        mockMvc.perform(post(FORGOT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("a@b.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST reset-password valid payload returns 200")
    void resetPassword_valid() throws Exception {
        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(new SimpleMessageResponse("Password reset successful"));

        mockMvc.perform(post(RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest("token", "NewStrong123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST reset-password expired token maps to 400")
    void resetPassword_expiredToken() throws Exception {
        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenThrow(new AuthValidationException("Reset token is expired"));

        mockMvc.perform(post(RESET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest("token", "NewStrong123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST change-password with authenticated principal returns 200")
    void changePassword_authenticated() throws Exception {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", UserRole.LISTENER);
        when(authService.changePassword(anyLong(), any(ChangePasswordRequest.class))).thenReturn(new SimpleMessageResponse("Password changed successfully"));

        mockMvc.perform(post(CHANGE_URL)
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("OldStrong1!", "NewStrong123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST change-password without JWT returns 403")
    void changePassword_noJwt() throws Exception {
        mockMvc.perform(post(CHANGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("OldStrong1!", "NewStrong123!"))))
                .andExpect(status().isForbidden());
    }

    private AuthTokenResponse tokenResponse() {
        UserResponse user = new UserResponse(1L, "a@b.com", "user123", "LISTENER", true, Instant.now(), Instant.now());
        return new AuthTokenResponse("Bearer", "access", 3600L, "refresh", 1209600L, user);
    }
}
