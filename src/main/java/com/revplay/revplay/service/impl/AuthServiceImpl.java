package com.revplay.revplay.service.impl;

import com.revplay.revplay.dto.request.*;
import com.revplay.revplay.dto.response.*;
import com.revplay.revplay.entity.PasswordResetToken;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.exception.*;
import com.revplay.revplay.repository.PasswordResetTokenRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.security.JwtUtil;
import com.revplay.revplay.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public ApiResponse<UserResponse> register(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already exists");

        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username already exists");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        UserResponse response = UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.isActive())
                .isEmailVerified(user.isEmailVerified())
                .build();

        log.info("User registered successfully: {}", user.getEmail());

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new UnauthorizedException("Invalid credentials");

        if (!user.isActive())
            throw new BadRequestException("Account is inactive");

        String token = jwtUtil.generateToken(user);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        log.info("User logged in: {}", user.getEmail());

        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(false);
        resetToken.setCreatedAt(LocalDateTime.now());

        tokenRepository.save(resetToken);

        log.info("Password reset token generated for: {}", user.getEmail());

        return ApiResponse.<String>builder()
                .success(true)
                .message("Password reset token generated")
                .data(token)
                .build();
    }

    @Override
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (resetToken.isUsed())
            throw new BadRequestException("Token already used");

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Token expired");

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        resetToken.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());

        return ApiResponse.<String>builder()
                .success(true)
                .message("Password reset successful")
                .data("Password updated")
                .build();
    }
}
