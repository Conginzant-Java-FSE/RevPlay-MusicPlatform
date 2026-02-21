package com.revplay.revplay.service.Impl;

import com.revplay.revplay.dto.request.UpdateUserProfile;
import com.revplay.revplay.dto.response.ApiResponse;
import com.revplay.revplay.dto.response.UserProfileResponse;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.entity.UserProfile;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.repository.UserProfileRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String PROFILE_NOT_FOUND = "User profile not found";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getAuthenticatedUserProfile(String email) {

        log.info("Fetching profile for authenticated user: {}", email);

        validateEmail(email);

        User user = findUserByEmail(email);
        UserProfile profile = findProfileByUserId(user.getUserId());

        UserProfileResponse response = mapToResponse(profile);

        log.info("Profile fetched successfully for userId: {}", user.getUserId());

        return buildSuccessResponse("Profile fetched successfully", response);
    }

    @Override
    public ApiResponse<UserProfileResponse> updateAuthenticatedUserProfile(
            String email,
            UpdateUserProfile request) {

        log.info("Updating profile for authenticated user: {}", email);

        validateEmail(email);
        validateUpdateRequest(request);

        User user = findUserByEmail(email);
        UserProfile profile = findProfileByUserId(user.getUserId());

        updateProfileFields(profile, request);

        userProfileRepository.save(profile);

        log.info("Profile updated successfully for userId: {}", user.getUserId());

        return buildSuccessResponse(
                "Profile updated successfully",
                mapToResponse(profile)
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new ResourceNotFoundException(USER_NOT_FOUND);
                });
    }

    private UserProfile findProfileByUserId(Long userId) {
        return userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> {
                    log.error("Profile not found for userId: {}", userId);
                    return new ResourceNotFoundException(PROFILE_NOT_FOUND);
                });
    }

    private void updateProfileFields(UserProfile profile, UpdateUserProfile request) {

        profile.setFullName(request.getFullName());
        profile.setBio(request.getBio());
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setUpdatedAt(LocalDateTime.now());
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {

        return UserProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private ApiResponse<UserProfileResponse> buildSuccessResponse(
            String message,
            UserProfileResponse data) {

        return ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    private void validateEmail(String email) {
        if (Objects.isNull(email) || email.isBlank()) {
            log.error("Invalid authentication context: email is null or blank");
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }
    }

    private void validateUpdateRequest(UpdateUserProfile request) {
        if (Objects.isNull(request)) {
            log.error("Update profile request is null");
            throw new IllegalArgumentException("Update request cannot be null");
        }
    }
}