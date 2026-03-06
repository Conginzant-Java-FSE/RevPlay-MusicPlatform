package com.revplay.musicplatform.premium.controller;

import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.exception.BadRequestException;
import com.revplay.musicplatform.premium.dto.PremiumStatusResponse;
import com.revplay.musicplatform.premium.service.SubscriptionService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(PremiumController.class)
@Import(SecurityConfig.class)
class PremiumControllerTest {

    private static final Long USER_ID = 100L;
    private static final String STATUS_URL = "/api/v1/premium/status";
    private static final String UPGRADE_URL = "/api/v1/premium/upgrade";

    private final MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    PremiumControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET premium status with authenticated user returns 200")
    void getStatus_authenticated_ok() throws Exception {
        when(subscriptionService.getPremiumStatus(USER_ID))
                .thenReturn(new PremiumStatusResponse(true, LocalDateTime.now().plusDays(1)));

        String body = mockMvc.perform(get(STATUS_URL)
                        .param("userId", String.valueOf(USER_ID))
                        .with(authentication(auth(USER_ID, UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(body).contains("expiryDate");
    }

    @Test
    @DisplayName("GET premium status without JWT returns 403")
    void getStatus_noJwt_forbidden() throws Exception {
        mockMvc.perform(get(STATUS_URL).param("userId", String.valueOf(USER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST premium upgrade monthly with authenticated user returns 200")
    void upgrade_monthly_authenticated_ok() throws Exception {
        doNothing().when(subscriptionService).upgradeToPremium(USER_ID, "MONTHLY");

                mockMvc.perform(post(UPGRADE_URL)
                        .param("userId", String.valueOf(USER_ID))
                        .param("planType", "MONTHLY")
                        .with(authentication(auth(USER_ID, UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST premium upgrade with invalid planType returns 400")
    void upgrade_invalidPlan_badRequest() throws Exception {
        doThrow(new BadRequestException("Unsupported planType. Use MONTHLY or YEARLY"))
                .when(subscriptionService).upgradeToPremium(USER_ID, "WEEKLY");

                mockMvc.perform(post(UPGRADE_URL)
                        .param("userId", String.valueOf(USER_ID))
                        .param("planType", "WEEKLY")
                        .with(authentication(auth(USER_ID, UserRole.LISTENER))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST premium upgrade without JWT returns 403")
    void upgrade_noJwt_forbidden() throws Exception {
        mockMvc.perform(post(UPGRADE_URL)
                        .param("userId", String.valueOf(USER_ID))
                        .param("planType", "MONTHLY"))
                .andExpect(status().isForbidden());
    }

    private UsernamePasswordAuthenticationToken auth(Long userId, UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "user" + userId, role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
