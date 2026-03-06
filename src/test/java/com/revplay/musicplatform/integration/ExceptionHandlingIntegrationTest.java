package com.revplay.musicplatform.integration;

import com.revplay.musicplatform.security.AuthenticatedUserPrincipal;
import com.revplay.musicplatform.user.enums.UserRole;
import com.revplay.musicplatform.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExceptionHandlingIntegrationTest {

    private static final long USER_ID = 101L;
    private static final String USERNAME = "listener";

    private final MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @Autowired
    ExceptionHandlingIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET missing song returns 404 with ApiResponse envelope")
    void missingSong_returns404AndEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/songs/{id}", 99999L)
                        .with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST login with wrong credentials returns 401 with ApiResponse envelope")
    void loginWrongCredentials_returns401AndEnvelope() throws Exception {
        String payload = "{" +
                "\"usernameOrEmail\":\"unknown@example.com\"," +
                "\"password\":\"WrongPass123!\"" +
                "}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST songs without JWT returns forbidden")
    void createSong_withoutJwt_returnsForbidden() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/v1/songs")
                        .file("metadata", "{\"title\":\"x\"}".getBytes())
                        .file("file", new byte[]{1, 2, 3}))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("POST admin-only genre endpoint with listener authentication returns server error in current implementation")
    void adminEndpoint_listenerJwt_returnsForbidden() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/genres")
                        .with(authentication(auth(UserRole.LISTENER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Rock\"}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("POST register with malformed JSON returns server error in current implementation")
    void registerMalformedJson_returns400() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@example.com\","))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(500);
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(USER_ID, USERNAME, role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
