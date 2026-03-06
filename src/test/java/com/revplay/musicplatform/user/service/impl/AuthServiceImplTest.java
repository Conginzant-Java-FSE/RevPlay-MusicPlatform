package com.revplay.musicplatform.user.service.impl;

import com.revplay.musicplatform.audit.enums.AuditActionType;
import com.revplay.musicplatform.audit.enums.AuditEntityType;
import com.revplay.musicplatform.audit.service.AuditLogService;
import com.revplay.musicplatform.common.TestDataFactory;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.JwtProperties;
import com.revplay.musicplatform.security.service.InMemoryRateLimiterService;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.dto.request.*;
import com.revplay.musicplatform.user.dto.response.AuthTokenResponse;
import com.revplay.musicplatform.user.dto.response.SimpleMessageResponse;
import com.revplay.musicplatform.user.entity.PasswordResetToken;
import com.revplay.musicplatform.user.entity.User;
import com.revplay.musicplatform.user.entity.UserProfile;
import com.revplay.musicplatform.user.enums.UserRole;
import com.revplay.musicplatform.user.exception.AuthConflictException;
import com.revplay.musicplatform.user.exception.AuthNotFoundException;
import com.revplay.musicplatform.user.exception.AuthUnauthorizedException;
import com.revplay.musicplatform.user.exception.AuthValidationException;
import com.revplay.musicplatform.user.repository.PasswordResetTokenRepository;
import com.revplay.musicplatform.user.repository.UserProfileRepository;
import com.revplay.musicplatform.user.repository.UserRepository;
import com.revplay.musicplatform.user.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "Strong123!";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String CLIENT_KEY = "127.0.0.1";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private TokenRevocationService tokenRevocationService;
    @Mock
    private InMemoryRateLimiterService inMemoryRateLimiterService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmailService emailService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                userProfileRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                jwtService,
                jwtProperties,
                tokenRevocationService,
                inMemoryRateLimiterService,
                auditLogService,
                emailService);
    }

    @Test
    @DisplayName("register happy path LISTENER role")
    void register_happyPath_listener() {
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD, TEST_FULL_NAME,
                "LISTENER");
        User savedUser = TestDataFactory.buildUser(1L, TEST_EMAIL, TEST_USERNAME, UserRole.LISTENER);
        savedUser.setEmailVerified(false);

        when(userRepository.existsByEmailIgnoreCase(TEST_EMAIL)).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        stubTokenGeneration(savedUser);

        AuthTokenResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        verify(userRepository, times(2)).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(emailService).sendEmail(eq(TEST_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("register ARTIST role in request")
    void register_artistRole() {
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD, TEST_FULL_NAME,
                "ARTIST");
        User savedUser = TestDataFactory.buildUser(1L, TEST_EMAIL, TEST_USERNAME, UserRole.ARTIST);

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        stubTokenGeneration(savedUser);

        authService.register(request);

        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    @DisplayName("register null request throws")
    void register_nullRequest() {
        assertThatThrownBy(() -> authService.register(null))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage("Register request is required");
    }

    @Test
    @DisplayName("register weak password throws")
    void register_weakPassword() {
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_USERNAME, "mypassword1", TEST_FULL_NAME,
                "LISTENER");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage("Password is too weak");
    }

    @Test
    @DisplayName("register email exists throws")
    void register_emailExists() {
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD, TEST_FULL_NAME,
                "LISTENER");
        when(userRepository.existsByEmailIgnoreCase(TEST_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthConflictException.class)
                .hasMessage("Email already exists");
    }

    @Test
    @DisplayName("register username exists throws")
    void register_usernameExists() {
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD, TEST_FULL_NAME,
                "LISTENER");
        when(userRepository.existsByEmailIgnoreCase(TEST_EMAIL)).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthConflictException.class)
                .hasMessage("Username already exists");
    }

    @Test
    @DisplayName("register emailService throws but registration succeeds")
    void register_emailThrows() {
        RegisterRequest request = new RegisterRequest(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD, TEST_FULL_NAME,
                "LISTENER");
        User savedUser = TestDataFactory.buildUser(1L, TEST_EMAIL, TEST_USERNAME, UserRole.LISTENER);

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doThrow(new RuntimeException("mail down")).when(emailService).sendEmail(anyString(), anyString(), anyString());
        stubTokenGeneration(savedUser);

        AuthTokenResponse response = authService.register(request);

        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("login happy path via email")
    void login_happyPath_email() {
        User user = userWithPasswordHash();
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, "hash")).thenReturn(true);
        stubTokenGeneration(user);

        AuthTokenResponse response = authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), CLIENT_KEY);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    @DisplayName("login happy path via username")
    void login_happyPath_username() {
        User user = userWithPasswordHash();
        when(userRepository.findByUsernameIgnoreCase(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, "hash")).thenReturn(true);
        stubTokenGeneration(user);

        AuthTokenResponse response = authService.login(new LoginRequest(TEST_USERNAME, TEST_PASSWORD), CLIENT_KEY);

        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("login user not found throws")
    void login_userNotFound() {
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), CLIENT_KEY))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("login unverified throws")
    void login_unverified() {
        User user = userWithPasswordHash();
        user.setEmailVerified(false);
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), CLIENT_KEY))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Please verify your email before logging in.");
    }

    @Test
    @DisplayName("login inactive throws")
    void login_inactive() {
        User user = userWithPasswordHash();
        user.setIsActive(false);
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), CLIENT_KEY))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Account is deactivated");
    }

    @Test
    @DisplayName("login wrong password throws")
    void login_wrongPassword() {
        User user = userWithPasswordHash();
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(TEST_EMAIL, "bad"), CLIENT_KEY))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("login calls rate limiter with expected args")
    void login_rateLimiterCalled() {
        User user = userWithPasswordHash();
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, "hash")).thenReturn(true);
        stubTokenGeneration(user);

        authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), CLIENT_KEY);

        verify(inMemoryRateLimiterService).ensureWithinLimit(
                eq("login:" + CLIENT_KEY),
                eq(5),
                eq(60),
                eq("Too many login attempts. Please try again later."));
    }

    @Test
    @DisplayName("refreshToken valid token returns new response")
    void refreshToken_valid() {
        User user = userWithPasswordHash();

        when(tokenRevocationService.isRevoked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtService.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.toPrincipal(REFRESH_TOKEN))
                .thenReturn(new AuthenticatedUserPrincipal(1L, TEST_USERNAME, UserRole.LISTENER));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        stubTokenGeneration(user);

        AuthTokenResponse response = authService.refreshToken(new RefreshTokenRequest(REFRESH_TOKEN));

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    @DisplayName("refreshToken revoked token throws")
    void refreshToken_revoked() {
        when(tokenRevocationService.isRevoked(REFRESH_TOKEN)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest(REFRESH_TOKEN)))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Refresh token is revoked");
    }

    @Test
    @DisplayName("refreshToken wrong type throws")
    void refreshToken_wrongType() {
        when(tokenRevocationService.isRevoked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtService.isRefreshToken(REFRESH_TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest(REFRESH_TOKEN)))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("refreshToken user not found throws")
    void refreshToken_userNotFound() {
        when(tokenRevocationService.isRevoked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtService.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.toPrincipal(REFRESH_TOKEN))
                .thenReturn(new AuthenticatedUserPrincipal(99L, TEST_USERNAME, UserRole.LISTENER));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest(REFRESH_TOKEN)))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("refreshToken inactive user throws")
    void refreshToken_inactive() {
        User user = userWithPasswordHash();
        user.setIsActive(false);

        when(tokenRevocationService.isRevoked(REFRESH_TOKEN)).thenReturn(false);
        when(jwtService.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.toPrincipal(REFRESH_TOKEN))
                .thenReturn(new AuthenticatedUserPrincipal(1L, TEST_USERNAME, UserRole.LISTENER));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest(REFRESH_TOKEN)))
                .isInstanceOf(AuthUnauthorizedException.class)
                .hasMessage("Account is deactivated");
    }

    @Test
    @DisplayName("logout valid token revokes and returns success")
    void logout_valid() {
        Instant expiry = Instant.now().plusSeconds(600);
        when(jwtService.getExpiry("token")).thenReturn(expiry);

        SimpleMessageResponse response = authService.logout("token");

        assertThat(response.message()).isEqualTo("Logged out successfully");
        verify(tokenRevocationService).revoke("token", expiry);
    }

    @Test
    @DisplayName("logout null token returns success")
    void logout_null() {
        SimpleMessageResponse response = authService.logout(null);

        assertThat(response.message()).isEqualTo("Logged out successfully");
        verify(tokenRevocationService, never()).revoke(anyString(), any());
    }

    @Test
    @DisplayName("logout blank token returns success")
    void logout_blank() {
        SimpleMessageResponse response = authService.logout("   ");

        assertThat(response.message()).isEqualTo("Logged out successfully");
        verify(tokenRevocationService, never()).revoke(anyString(), any());
    }

    @Test
    @DisplayName("forgotPassword happy path")
    void forgotPassword_happyPath() {
        User user = userWithPasswordHash();
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        SimpleMessageResponse response = authService.forgotPassword(new ForgotPasswordRequest(TEST_EMAIL), CLIENT_KEY);

        assertThat(response.message()).isEqualTo("Password reset email sent successfully");
        verify(passwordResetTokenRepository).deleteByExpiryDateBefore(any(Instant.class));
        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendEmail(eq(TEST_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("forgotPassword user not found throws")
    void forgotPassword_userNotFound() {
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(new ForgotPasswordRequest(TEST_EMAIL), CLIENT_KEY))
                .isInstanceOf(AuthNotFoundException.class);
    }

    @Test
    @DisplayName("forgotPassword email send failure swallowed")
    void forgotPassword_emailThrows() {
        User user = userWithPasswordHash();
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("mail down")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        SimpleMessageResponse response = authService.forgotPassword(new ForgotPasswordRequest(TEST_EMAIL), CLIENT_KEY);

        assertThat(response.message()).isEqualTo("Password reset email sent successfully");
    }

    @Test
    @DisplayName("forgotPassword rate limiter key")
    void forgotPassword_rateLimiter() {
        User user = userWithPasswordHash();
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.forgotPassword(new ForgotPasswordRequest(TEST_EMAIL), CLIENT_KEY);

        verify(inMemoryRateLimiterService).ensureWithinLimit(
                eq("forgot-password:" + CLIENT_KEY),
                eq(3),
                eq(600),
                eq("Too many forgot-password requests. Please try again later."));
    }

    @Test
    @DisplayName("resetPassword happy path")
    void resetPassword_happyPath() {
        User user = userWithPasswordHash();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken("valid");
        token.setExpiryDate(Instant.now().plusSeconds(300));

        when(passwordResetTokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewStrong123!")).thenReturn("new-hash");

        SimpleMessageResponse response = authService.resetPassword(new ResetPasswordRequest("valid", "NewStrong123!"));

        assertThat(response.message()).isEqualTo("Password reset successful");
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(token);
        verify(auditLogService).logInternal(eq(AuditActionType.PASSWORD_RESET), anyLong(), eq(AuditEntityType.USER),
                anyLong(), anyString());
    }

    @Test
    @DisplayName("resetPassword token not found throws")
    void resetPassword_tokenNotFound() {
        when(passwordResetTokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("bad", "NewStrong123!")))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage("Invalid reset token");
    }

    @Test
    @DisplayName("resetPassword expired token deletes then throws")
    void resetPassword_expired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired");
        token.setExpiryDate(Instant.now().minusSeconds(1));
        token.setUser(userWithPasswordHash());
        when(passwordResetTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("expired", "NewStrong123!")))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage("Reset token is expired");
        verify(passwordResetTokenRepository).delete(token);
    }

    @Test
    @DisplayName("changePassword happy path")
    void changePassword_happyPath() {
        User user = userWithPasswordHash();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hash")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("new-hash");

        SimpleMessageResponse response = authService.changePassword(1L, new ChangePasswordRequest("old", "new"));

        assertThat(response.message()).isEqualTo("Password changed successfully");
        verify(userRepository).save(user);
        verify(auditLogService).logInternal(eq(AuditActionType.PASSWORD_CHANGE), eq(1L), eq(AuditEntityType.USER),
                eq(1L), anyString());
    }

    @Test
    @DisplayName("changePassword user not found")
    void changePassword_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(1L, new ChangePasswordRequest("old", "new")))
                .isInstanceOf(AuthNotFoundException.class);
    }

    @Test
    @DisplayName("changePassword wrong current password")
    void changePassword_wrongCurrent() {
        User user = userWithPasswordHash();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, new ChangePasswordRequest("bad", "new")))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage("Current password is incorrect");
    }

    @Test
    @DisplayName("verifyEmailOtp happy path")
    void verifyEmailOtp_happyPath() {
        User user = userWithPasswordHash();
        user.setEmailVerified(false);
        user.setEmailOtp("123456");
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(2));
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        SimpleMessageResponse response = authService.verifyEmailOtp(TEST_EMAIL, "123456");

        assertThat(response.message()).isEqualTo("Email verified successfully");
        assertThat(user.getEmailVerified()).isTrue();
        assertThat(user.getEmailOtp()).isNull();
        verify(emailService).sendWelcomeEmail(TEST_EMAIL, TEST_USERNAME);
    }

    @Test
    @DisplayName("verifyEmailOtp already verified")
    void verifyEmailOtp_alreadyVerified() {
        User user = userWithPasswordHash();
        user.setEmailVerified(true);
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        SimpleMessageResponse response = authService.verifyEmailOtp(TEST_EMAIL, "123456");

        assertThat(response.message()).isEqualTo("Email already verified");
    }

    @Test
    @DisplayName("verifyEmailOtp wrong otp")
    void verifyEmailOtp_wrongOtp() {
        User user = userWithPasswordHash();
        user.setEmailVerified(false);
        user.setEmailOtp("999999");
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(1));
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmailOtp(TEST_EMAIL, "123456"))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage("Invalid OTP");
    }

    @Test
    @DisplayName("verifyEmailOtp expired otp")
    void verifyEmailOtp_expiredOtp() {
        User user = userWithPasswordHash();
        user.setEmailVerified(false);
        user.setEmailOtp("123456");
        user.setOtpExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmailOtp(TEST_EMAIL, "123456"))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage("OTP expired");
    }

    @Test
    @DisplayName("verifyEmailOtp welcome mail throws but still succeeds")
    void verifyEmailOtp_welcomeThrows() {
        User user = userWithPasswordHash();
        user.setEmailVerified(false);
        user.setEmailOtp("123456");
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(1));
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("mail down")).when(emailService).sendWelcomeEmail(anyString(), anyString());

        SimpleMessageResponse response = authService.verifyEmailOtp(TEST_EMAIL, "123456");

        assertThat(response.message()).isEqualTo("Email verified successfully");
    }

    @Test
    @DisplayName("resendEmailOtp happy path")
    void resendEmailOtp_happyPath() {
        User user = userWithPasswordHash();
        user.setEmailVerified(false);
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        SimpleMessageResponse response = authService.resendEmailOtp(TEST_EMAIL);

        assertThat(response.message()).isEqualTo("OTP sent successfully");
        verify(userRepository).save(user);
        verify(emailService).sendEmail(eq(TEST_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("resendEmailOtp already verified")
    void resendEmailOtp_alreadyVerified() {
        User user = userWithPasswordHash();
        user.setEmailVerified(true);
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));

        SimpleMessageResponse response = authService.resendEmailOtp(TEST_EMAIL);

        assertThat(response.message()).isEqualTo("Email already verified");
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("resendEmailOtp email send failure swallowed")
    void resendEmailOtp_emailThrows() {
        User user = userWithPasswordHash();
        user.setEmailVerified(false);
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("mail down")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        SimpleMessageResponse response = authService.resendEmailOtp(TEST_EMAIL);

        assertThat(response.message()).isEqualTo("OTP sent successfully");
    }

    private User userWithPasswordHash() {
        User user = TestDataFactory.buildUser(1L, TEST_EMAIL, TEST_USERNAME, UserRole.LISTENER);
        user.setPasswordHash("hash");
        user.setEmailVerified(true);
        user.setIsActive(true);
        return user;
    }

    private void stubTokenGeneration(User user) {
        when(jwtService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);
        when(jwtService.getExpiry(ACCESS_TOKEN)).thenReturn(Instant.now().plusSeconds(600));
        when(jwtService.getExpiry(REFRESH_TOKEN)).thenReturn(Instant.now().plusSeconds(1200));
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(1209600L);
    }
}
