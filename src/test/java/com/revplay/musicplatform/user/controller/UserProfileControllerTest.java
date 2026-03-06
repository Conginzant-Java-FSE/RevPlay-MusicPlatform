package com.revplay.musicplatform.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.dto.request.UpdateProfileRequest;
import com.revplay.musicplatform.user.dto.response.UserProfileResponse;
import com.revplay.musicplatform.user.enums.UserRole;
import com.revplay.musicplatform.user.service.UserProfileService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(UserProfileController.class)
@Import(SecurityConfig.class)
class UserProfileControllerTest {

    private static final String PROFILE_URL = "/api/v1/profile/{userId}";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    UserProfileControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("GET profile authenticated user returns 200 and fields")
    void getProfile_authenticated() throws Exception {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", UserRole.LISTENER);
        when(userProfileService.getProfile(eq(1L), any())).thenReturn(new UserProfileResponse(1L, "User Name", "Bio", "img", "IN"));

        mockMvc.perform(get(PROFILE_URL, 1L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("User Name"));
    }

    @Test
    @DisplayName("GET profile without JWT returns 403")
    void getProfile_noJwt() throws Exception {
        mockMvc.perform(get(PROFILE_URL, 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT profile valid update returns 200")
    void updateProfile_valid() throws Exception {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", UserRole.LISTENER);
        UpdateProfileRequest request = new UpdateProfileRequest("Updated Name", "New Bio", "img2", "US");
        when(userProfileService.updateProfile(eq(1L), any(UpdateProfileRequest.class), any()))
                .thenReturn(new UserProfileResponse(1L, "Updated Name", "New Bio", "img2", "US"));

        mockMvc.perform(put(PROFILE_URL, 1L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"));
    }
}
