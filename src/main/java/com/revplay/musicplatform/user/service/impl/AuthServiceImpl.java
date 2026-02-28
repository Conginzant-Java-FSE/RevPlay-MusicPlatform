package com.revplay.musicplatform.user.service.impl;


import com.revplay.musicplatform.security.JwtProperties;
import com.revplay.musicplatform.user.service.AuthService;
import com.revplay.musicplatform.user.dto.request.ChangePasswordRequest;
import com.revplay.musicplatform.user.dto.request.ForgotPasswordRequest;
import com.revplay.musicplatform.user.dto.request.LoginRequest;
import com.revplay.musicplatform.user.dto.request.RefreshTokenRequest;
import com.revplay.musicplatform.user.dto.request.RegisterRequest;
import com.revplay.musicplatform.user.dto.request.ResetPasswordRequest;
import com.revplay.musicplatform.user.dto.response.AuthTokenResponse;
import com.revplay.musicplatform.user.dto.response.ForgotPasswordResponse;
import com.revplay.musicplatform.user.dto.response.SimpleMessageResponse;
import com.revplay.musicplatform.user.dto.response.UserResponse;
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
import com.revplay.musicplatform.audit.enums.AuditActionType;
import com.revplay.musicplatform.audit.enums.AuditEntityType;
import com.revplay.musicplatform.audit.service.AuditLogService;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.service.InMemoryRateLimiterService;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;
    private final InMemoryRateLimiterService inMemoryRateLimiterService;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            TokenRevocationService tokenRevocationService,
            InMemoryRateLimiterService inMemoryRateLimiterService,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenRevocationService = tokenRevocationService;
        this.inMemoryRateLimiterService = inMemoryRateLimiterService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        LOGGER.info("Registering user with username={}", request == null ? null : request.username());
        validateRegisterRequest(request);

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AuthConflictException("Email already exists");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new AuthConflictException("Username already exists");
        }

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(resolveRequestedRole(request.role()));
        user.setIsActive(Boolean.TRUE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(savedUser.getUserId());
        profile.setFullName(request.fullName().trim());
        profile.setBio(null);
        profile.setProfilePictureUrl(null);
        profile.setCountry(null);
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        userProfileRepository.save(profile);

        return buildTokenResponse(savedUser);
    }

    public AuthTokenResponse login(LoginRequest request, String clientKey) {
        LOGGER.info("Processing login request for client={}", normalizeClientKey(clientKey));
        inMemoryRateLimiterService.ensureWithinLimit(
                "login:" + normalizeClientKey(clientKey),
                5,
                60,
                "Too many login attempts. Please try again later."
        );
        User user = resolveUserByUsernameOrEmail(request.usernameOrEmail())
                .orElseThrow(() -> new AuthUnauthorizedException("Invalid credentials"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthUnauthorizedException("Account is deactivated");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthUnauthorizedException("Invalid credentials");
        }
        return buildTokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse refreshToken(RefreshTokenRequest request) {
        LOGGER.info("Refreshing token");
        if (tokenRevocationService.isRevoked(request.refreshToken())) {
            throw new AuthUnauthorizedException("Refresh token is revoked");
        }
        if (!jwtService.isRefreshToken(request.refreshToken())) {
            throw new AuthUnauthorizedException("Invalid refresh token");
        }
        AuthenticatedUserPrincipal principal = jwtService.toPrincipal(request.refreshToken());
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new AuthUnauthorizedException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthUnauthorizedException("Account is deactivated");
        }
        return buildTokenResponse(user);
    }

    public SimpleMessageResponse logout(String bearerToken) {
        LOGGER.info("Processing logout request");
        if (bearerToken != null && !bearerToken.isBlank()) {
            tokenRevocationService.revoke(bearerToken, jwtService.getExpiry(bearerToken));
        }
        return new SimpleMessageResponse("Logged out successfully");
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request, String clientKey) {
        LOGGER.info("Processing forgot-password for email={}", request == null ? null : request.email());
        inMemoryRateLimiterService.ensureWithinLimit(
                "forgot-password:" + normalizeClientKey(clientKey),
                3,
                600,
                "Too many forgot-password requests. Please try again later."
        );
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new AuthNotFoundException("User not found for the given email"));

        passwordResetTokenRepository.deleteByExpiresAtBeforeOrUsedAtIsNotNull(Instant.now());

        PasswordResetToken tokenEntity = new PasswordResetToken();
        tokenEntity.setUserId(user.getUserId());
        tokenEntity.setToken(generateResetToken());
        tokenEntity.setCreatedAt(Instant.now());
        tokenEntity.setExpiresAt(Instant.now().plusSeconds(3600));
        tokenEntity.setUsedAt(null);
        PasswordResetToken savedToken = passwordResetTokenRepository.save(tokenEntity);

        return new ForgotPasswordResponse(
                "Password reset token generated",
                savedToken.getToken(),
                savedToken.getExpiresAt()
        );
    }

    @Transactional
    public SimpleMessageResponse resetPassword(ResetPasswordRequest request) {
        LOGGER.info("Processing password reset by token");
        PasswordResetToken tokenEntity = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new AuthValidationException("Invalid reset token"));
        if (tokenEntity.getUsedAt() != null || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthValidationException("Reset token is expired or already used");
        }

        User user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new AuthNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        tokenEntity.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(tokenEntity);

        auditLogService.logInternal(
                AuditActionType.PASSWORD_RESET,
                user.getUserId(),
                AuditEntityType.USER,
                user.getUserId(),
                "Password reset completed via reset token"
        );

        return new SimpleMessageResponse("Password reset successful");
    }

    @Transactional
    public SimpleMessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        LOGGER.info("Processing password change for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AuthValidationException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        auditLogService.logInternal(
                AuditActionType.PASSWORD_CHANGE,
                userId,
                AuditEntityType.USER,
                userId,
                "Password changed by authenticated user"
        );

        return new SimpleMessageResponse("Password changed successfully");
    }

    private AuthTokenResponse buildTokenResponse(User user) {
        tokenRevocationService.revokeAllForUser(user.getUserId());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        tokenRevocationService.registerIssuedToken(user.getUserId(), accessToken, jwtService.getExpiry(accessToken));
        tokenRevocationService.registerIssuedToken(user.getUserId(), refreshToken, jwtService.getExpiry(refreshToken));

        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                jwtProperties.getAccessTokenExpirationSeconds(),
                refreshToken,
                jwtProperties.getRefreshTokenExpirationSeconds(),
                toUserResponse(user)
        );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Optional<User> resolveUserByUsernameOrEmail(String usernameOrEmail) {
        String input = usernameOrEmail.trim();
        if (input.contains("@")) {
            return userRepository.findByEmailIgnoreCase(input);
        }
        return userRepository.findByUsernameIgnoreCase(input);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new AuthValidationException("Register request is required");
        }
        if (request.password() != null && request.password().toLowerCase().contains("password")) {
            throw new AuthValidationException("Password is too weak");
        }
    }

    private String generateResetToken() {
        String seed = UUID.randomUUID() + ":" + Instant.now();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(seed.getBytes());
    }

    private String normalizeClientKey(String clientKey) {
        return (clientKey == null || clientKey.isBlank()) ? "unknown" : clientKey.trim();
    }

    private UserRole resolveRequestedRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return UserRole.LISTENER;
        }
        return UserRole.from(requestedRole);
    }
}




