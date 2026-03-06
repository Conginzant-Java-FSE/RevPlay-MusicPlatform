package com.revplay.musicplatform.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.security.SecurityConfig;
import com.revplay.musicplatform.security.service.JwtService;
import com.revplay.musicplatform.security.service.TokenRevocationService;
import com.revplay.musicplatform.user.dto.request.ResendOtpRequest;
import com.revplay.musicplatform.user.dto.request.VerifyEmailOtpRequest;
import com.revplay.musicplatform.user.dto.response.SimpleMessageResponse;
import com.revplay.musicplatform.user.exception.AuthNotFoundException;
import com.revplay.musicplatform.user.exception.AuthValidationException;
import com.revplay.musicplatform.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AuthVerificationController.class)
@Import(SecurityConfig.class)
class AuthVerificationControllerTest {

    private static final String VERIFY_URL = "/api/v1/auth/verify-email";
    private static final String RESEND_URL = "/api/v1/auth/resend-otp";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    AuthVerificationControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST verify-email valid OTP returns 200")
    void verifyEmail_validOtp() throws Exception {
        when(authService.verifyEmailOtp(anyString(), anyString())).thenReturn(new SimpleMessageResponse("Email verified successfully"));

        mockMvc.perform(post(VERIFY_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailOtpRequest("a@b.com", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST verify-email invalid OTP returns 400")
    void verifyEmail_invalidOtp() throws Exception {
        when(authService.verifyEmailOtp(anyString(), anyString())).thenThrow(new AuthValidationException("Invalid OTP"));

        mockMvc.perform(post(VERIFY_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailOtpRequest("a@b.com", "000000"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST resend-otp valid email returns 200")
    void resendOtp_validEmail() throws Exception {
        when(authService.resendEmailOtp(anyString())).thenReturn(new SimpleMessageResponse("OTP sent successfully"));

        mockMvc.perform(post(RESEND_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResendOtpRequest("a@b.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST resend-otp unknown email returns 404")
    void resendOtp_unknownEmail() throws Exception {
        when(authService.resendEmailOtp(anyString())).thenThrow(new AuthNotFoundException("User not found"));

        mockMvc.perform(post(RESEND_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResendOtpRequest("x@y.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
