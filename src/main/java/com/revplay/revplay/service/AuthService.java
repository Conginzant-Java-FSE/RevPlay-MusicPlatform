package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.*;
import com.revplay.revplay.dto.response.*;

public interface AuthService {

    ApiResponse<UserResponse> register(UserRequest request);

    ApiResponse<LoginResponse> login(LoginRequest request);

    ApiResponse<String> forgotPassword(ForgotPasswordRequest request);

    ApiResponse<String> resetPassword(ResetPasswordRequest request);
}
