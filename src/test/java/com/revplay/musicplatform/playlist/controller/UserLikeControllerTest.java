package com.revplay.musicplatform.playlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.playlist.dto.request.LikeRequest;
import com.revplay.musicplatform.playlist.dto.response.UserLikeResponse;
import com.revplay.musicplatform.playlist.service.UserLikeService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(UserLikeController.class)
@Import(SecurityConfig.class)
class UserLikeControllerTest {

    private static final String BASE = "/api/v1/likes";
    private static final Long USER_ID = 101L;
    private static final Long LIKEABLE_ID = 555L;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private UserLikeService userLikeService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    UserLikeControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @DisplayName("POST /api/v1/likes authenticated returns 201")
    void like_authenticated_returns201() throws Exception {
        LikeRequest request = new LikeRequest();
        request.setLikeableId(LIKEABLE_ID);
        request.setLikeableType("SONG");

        when(userLikeService.likeContent(any(LikeRequest.class)))
                .thenReturn(UserLikeResponse.builder().id(1L).userId(USER_ID).likeableId(LIKEABLE_ID).build());

        mockMvc.perform(post(BASE)
                        .with(authUser(USER_ID, UserRole.LISTENER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.likeableId").value(LIKEABLE_ID));
    }

    @Test
    @DisplayName("POST /api/v1/likes without auth returns forbidden")
    void like_noAuth_forbidden() throws Exception {
        LikeRequest request = new LikeRequest();
        request.setLikeableId(LIKEABLE_ID);
        request.setLikeableType("SONG");

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/likes/{userId} authenticated returns paginated response")
    void getLikes_authenticated_returns200() throws Exception {
        PagedResponseDto<UserLikeResponse> page = new PagedResponseDto<>(
                List.of(UserLikeResponse.builder().id(1L).userId(USER_ID).likeableId(LIKEABLE_ID).build()),
                0, 10, 1, 1, null, null
        );
        when(userLikeService.getUserLikes(eq(USER_ID), eq(null), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get(BASE + "/{userId}", USER_ID)
                        .with(authUser(USER_ID, UserRole.LISTENER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(USER_ID));

        verify(userLikeService).getUserLikes(USER_ID, null, 0, 10);
    }

    private RequestPostProcessor authUser(Long userId, UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "user-" + userId, role);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
