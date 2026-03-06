package com.revplay.musicplatform.security;

import com.revplay.musicplatform.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    private static final String INVALID_REGISTER_JSON = "{}";

    private final MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @Autowired
    SecurityConfigTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("public endpoints are accessible without JWT and not blocked by security")
    void publicEndpoints_withoutJwt_notForbidden() throws Exception {
        assertNotForbidden(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(INVALID_REGISTER_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));

        assertNotForbidden(get("/api/v1/search").param("q", "x"));
        assertNotForbidden(get("/api/v1/genres"));
        assertNotForbidden(get("/api/v1/browse/songs"));
        assertNotForbidden(get("/api/v1/discover/weekly/{userId}", 1L));
    }

    @Test
    @DisplayName("protected endpoints without JWT return forbidden")
    void protectedEndpoints_withoutJwt_forbidden() throws Exception {
        assertThat(mockMvc.perform(get("/api/v1/profile/{userId}", 1L)).andReturn().getResponse().getStatus())
                .isEqualTo(403);
        assertThat(mockMvc.perform(post("/api/v1/playlists").contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn().getResponse().getStatus())
                .isEqualTo(403);
        assertThat(mockMvc.perform(post("/api/v1/artists").contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn().getResponse().getStatus())
                .isEqualTo(403);
    }

    private void assertNotForbidden(org.springframework.test.web.servlet.RequestBuilder requestBuilder) throws Exception {
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
    }
}
