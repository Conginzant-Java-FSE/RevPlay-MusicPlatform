package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.UpdateUserProfile;
import com.revplay.revplay.dto.response.ApiResponse;
import com.revplay.revplay.dto.response.UserProfileResponse;
import com.revplay.revplay.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();
        log.info("GET /users/me requested by {}", email);

        return ResponseEntity.ok(
                userService.getAuthenticatedUserProfile(email)
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication authentication,
            @RequestBody UpdateUserProfile request) {

        String email = authentication.getName();
        log.info("PUT /users/profile requested by {}", email);

        return ResponseEntity.ok(
                userService.updateAuthenticatedUserProfile(email, request)
        );
    }
}