package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.UserProfileResponse;
import com.revplay.revplay.dto.response.UserResponse;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.entity.UserProfile;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toUserResponse(User user) {

        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.isActive())
                .isEmailVerified(user.isEmailVerified())
                .build();
    }

    public static UserProfileResponse toProfileResponse(UserProfile profile) {

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
}