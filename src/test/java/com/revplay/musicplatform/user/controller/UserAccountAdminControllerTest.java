package com.revplay.musicplatform.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.dto.request.UpdateUserRoleRequest;
import com.revplay.musicplatform.user.dto.request.UpdateUserStatusRequest;
import com.revplay.musicplatform.user.dto.response.SimpleMessageResponse;
import com.revplay.musicplatform.user.enums.UserRole;
import com.revplay.musicplatform.user.service.UserAccountAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(UserAccountAdminController.class)
@Import(SecurityConfig.class)
class UserAccountAdminControllerTest {

    private static final String STATUS_URL = "/api/v1/admin/users/{userId}/status";
    private static final String ROLE_URL = "/api/v1/admin/users/{userId}/role";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private UserAccountAdminService userAccountAdminService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    UserAccountAdminControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("PATCH status as ADMIN returns 200")
    void updateStatus_admin() throws Exception {
        AuthenticatedUserPrincipal admin = new AuthenticatedUserPrincipal(1L, "admin", UserRole.ADMIN);
        when(userAccountAdminService.updateStatus(eq(2L), any(UpdateUserStatusRequest.class), any()))
                .thenReturn(new SimpleMessageResponse("Status updated"));

        mockMvc.perform(patch(STATUS_URL, 2L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                admin,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH status as LISTENER returns 500 due global exception mapping")
    void updateStatus_listenerForbidden() throws Exception {
        AuthenticatedUserPrincipal listener = new AuthenticatedUserPrincipal(1L, "listener", UserRole.LISTENER);

        mockMvc.perform(patch(STATUS_URL, 2L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                listener,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_LISTENER")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest(false))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("PATCH status without JWT returns 403")
    void updateStatus_noJwt() throws Exception {
        mockMvc.perform(patch(STATUS_URL, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest(false))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH role as ADMIN returns 200")
    void updateRole_admin() throws Exception {
        AuthenticatedUserPrincipal admin = new AuthenticatedUserPrincipal(1L, "admin", UserRole.ADMIN);
        when(userAccountAdminService.updateRole(eq(2L), any(UpdateUserRoleRequest.class), any()))
                .thenReturn(new SimpleMessageResponse("Role updated"));

        mockMvc.perform(patch(ROLE_URL, 2L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                admin,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRoleRequest("ARTIST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH role as LISTENER returns 500 due global exception mapping")
    void updateRole_listenerForbidden() throws Exception {
        AuthenticatedUserPrincipal listener = new AuthenticatedUserPrincipal(1L, "listener", UserRole.LISTENER);

        mockMvc.perform(patch(ROLE_URL, 2L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                listener,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_LISTENER")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRoleRequest("ARTIST"))))
                .andExpect(status().isInternalServerError());
    }
}
