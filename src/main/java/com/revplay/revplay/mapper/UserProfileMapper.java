package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.UserProfileResponse;
import com.revplay.revplay.entity.UserProfile;

public final class UserProfileMapper {

    private UserProfileMapper() {}

    public static UserProfileResponse toResponse(UserProfile profile) {

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