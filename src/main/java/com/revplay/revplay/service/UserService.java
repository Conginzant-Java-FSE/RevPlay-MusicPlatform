package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.UpdateUserProfile;
import com.revplay.revplay.dto.response.ApiResponse;
import com.revplay.revplay.dto.response.UserProfileResponse;

public interface UserService {

    ApiResponse<UserProfileResponse> getAuthenticatedUserProfile(String email);

    ApiResponse<UserProfileResponse> updateAuthenticatedUserProfile(
            String email,
            UpdateUserProfile request
    );
}