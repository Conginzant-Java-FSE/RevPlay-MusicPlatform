package com.revplay.musicplatform.user.service;

import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.user.dto.request.UpdateUserRoleRequest;
import com.revplay.musicplatform.user.dto.request.UpdateUserStatusRequest;
import com.revplay.musicplatform.user.dto.response.SimpleMessageResponse;

public interface UserAccountAdminService {

    SimpleMessageResponse updateStatus(Long userId, UpdateUserStatusRequest request, AuthenticatedUserPrincipal admin);

    SimpleMessageResponse updateRole(Long userId, UpdateUserRoleRequest request, AuthenticatedUserPrincipal admin);
}


