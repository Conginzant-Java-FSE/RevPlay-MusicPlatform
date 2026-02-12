package com.revplay.revplay.dto.response;

import com.revplay.revplay.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long userId;
    private String email;
    private String username;
    private Role role;
    private boolean isActive;
    private boolean isEmailVerified;
}
