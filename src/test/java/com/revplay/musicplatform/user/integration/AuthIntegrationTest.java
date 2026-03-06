package com.revplay.musicplatform.user.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.user.dto.request.LoginRequest;
import com.revplay.musicplatform.user.dto.request.RefreshTokenRequest;
import com.revplay.musicplatform.user.dto.request.RegisterRequest;
import com.revplay.musicplatform.user.dto.request.VerifyEmailOtpRequest;
import com.revplay.musicplatform.user.entity.User;
import com.revplay.musicplatform.user.repository.UserRepository;
import com.revplay.musicplatform.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

        private static final String REGISTER_URL = "/api/v1/auth/register";
        private static final String LOGIN_URL = "/api/v1/auth/login";
        private static final String VERIFY_URL = "/api/v1/auth/verify-email";
        private static final String REFRESH_URL = "/api/v1/auth/refresh";
        private static final String LOGOUT_URL = "/api/v1/auth/logout";

        private final MockMvc mockMvc;
        private final ObjectMapper objectMapper;
        private final UserRepository userRepository;

        @MockBean
        private EmailService emailService;

        @Autowired
        AuthIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper, UserRepository userRepository) {
                this.mockMvc = mockMvc;
                this.objectMapper = objectMapper;
                this.userRepository = userRepository;
        }

        @Test
        @DisplayName("Register -> verify OTP -> login -> protected endpoint returns 200")
        void registerVerifyLoginProtectedFlow() throws Exception {
                register("flow1@test.com", "flow1user");
                User user = userRepository.findByEmailIgnoreCase("flow1@test.com").orElseThrow();

                mockMvc.perform(post(VERIFY_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new VerifyEmailOtpRequest(user.getEmail(), user.getEmailOtp()))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                String accessToken = loginAndGetTokens("flow1@test.com", "Strong123!").get("access");

                mockMvc.perform(get("/api/v1/profile/{userId}", user.getUserId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Register duplicate email returns 409")
        void registerDuplicateEmail() throws Exception {
                register("dup@test.com", "dupuser");

                mockMvc.perform(post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new RegisterRequest("dup@test.com",
                                                "otheruser", "Strong123!", "Dup User", "LISTENER"))))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Login unverified account returns 401 in current implementation")
        void loginUnverifiedUnauthorized() throws Exception {
                register("unverified@test.com", "unverified");

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new LoginRequest("unverified@test.com", "Strong123!"))))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Login -> logout -> reuse access token returns 403")
        void logoutRevokesAccessToken() throws Exception {
                registerAndVerify("logout@test.com", "logoutuser");
                User user = userRepository.findByEmailIgnoreCase("logout@test.com").orElseThrow();

                String accessToken = loginAndGetTokens("logout@test.com", "Strong123!").get("access");

                mockMvc.perform(post(LOGOUT_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/v1/profile/{userId}", user.getUserId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Login -> refresh -> new access token works on protected endpoint")
        void refreshCreatesWorkingAccessToken() throws Exception {
                registerAndVerify("refresh@test.com", "refreshuser");
                User user = userRepository.findByEmailIgnoreCase("refresh@test.com").orElseThrow();

                java.util.Map<String, String> tokens = loginAndGetTokens("refresh@test.com", "Strong123!");

                MvcResult refreshResult = mockMvc.perform(post(REFRESH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                                .writeValueAsString(new RefreshTokenRequest(tokens.get("refresh")))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andReturn();

                String newAccess = objectMapper.readTree(refreshResult.getResponse().getContentAsString()).path("data")
                                .path("accessToken").asText();
                assertThat(newAccess).isNotBlank();

                mockMvc.perform(get("/api/v1/profile/{userId}", user.getUserId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Refresh endpoint with access token returns 401")
        void refreshWithAccessTokenUnauthorized() throws Exception {
                registerAndVerify("wrongrefresh@test.com", "wrongrefresh");
                java.util.Map<String, String> tokens = loginAndGetTokens("wrongrefresh@test.com", "Strong123!");

                mockMvc.perform(post(REFRESH_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper
                                                .writeValueAsString(new RefreshTokenRequest(tokens.get("access")))))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false));
        }

        private void register(String email, String username) throws Exception {
                mockMvc.perform(post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new RegisterRequest(email, username,
                                                "Strong123!", "User Name", "LISTENER"))))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true));
        }

        private void registerAndVerify(String email, String username) throws Exception {
                register(email, username);
                User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();

                mockMvc.perform(post(VERIFY_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new VerifyEmailOtpRequest(user.getEmail(), user.getEmailOtp()))))
                                .andExpect(status().isOk());
        }

        private java.util.Map<String, String> loginAndGetTokens(String usernameOrEmail, String password)
                        throws Exception {
                MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest(usernameOrEmail, password))))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode data = objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("data");
                return java.util.Map.of(
                                "access", data.path("accessToken").asText(),
                                "refresh", data.path("refreshToken").asText());
        }
}
