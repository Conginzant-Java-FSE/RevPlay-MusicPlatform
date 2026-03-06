package com.revplay.musicplatform.analytics.controller;

import com.revplay.musicplatform.analytics.dto.response.UserStatisticsResponse;
import com.revplay.musicplatform.analytics.service.UserStatisticsService;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(UserStatisticsController.class)
@Import(SecurityConfig.class)
class UserStatisticsControllerTest {

    private static final Long USER_ID = 101L;
    private static final String GET_URL = "/api/v1/user-statistics/{userId}";

    private final MockMvc mockMvc;

    @MockBean
    private UserStatisticsService userStatisticsService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    UserStatisticsControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET user statistics authenticated returns 200")
    void get_authenticated_ok() throws Exception {
        when(userStatisticsService.getByUserId(USER_ID))
                .thenReturn(new UserStatisticsResponse(USER_ID, 1L, 2L, 3L, 4L, Instant.now()));

        mockMvc.perform(get(GET_URL, USER_ID).with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.totalSongsPlayed").value(4));
    }

    @Test
    @DisplayName("GET user statistics without auth returns 403")
    void get_noAuth_forbidden() throws Exception {
        mockMvc.perform(get(GET_URL, USER_ID))
                .andExpect(status().isForbidden());
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "user", role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
