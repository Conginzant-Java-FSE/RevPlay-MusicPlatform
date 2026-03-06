package com.revplay.musicplatform.download.controller;

import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.download.service.DownloadService;
import com.revplay.musicplatform.exception.AccessDeniedException;
import com.revplay.musicplatform.exception.ResourceNotFoundException;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(DownloadController.class)
@Import(SecurityConfig.class)
class DownloadControllerTest {

    private static final Long USER_ID = 11L;
    private static final Long SONG_ID = 22L;
    private static final String URL = "/api/v1/download/song/{songId}";
    private static final String FILE_NAME = "My-Song.mp3";

    private final MockMvc mockMvc;

    @MockBean
    private DownloadService downloadService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    DownloadControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET download song for premium user returns file response")
    void downloadSong_premiumUser_returnsFile() throws Exception {
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(downloadService.downloadSong(USER_ID, SONG_ID)).thenReturn(resource);
        when(downloadService.getDownloadFileName(SONG_ID)).thenReturn(FILE_NAME);

        mockMvc.perform(get(URL, SONG_ID)
                        .param("userId", String.valueOf(USER_ID))
                        .with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + FILE_NAME + "\""));
    }

    @Test
    @DisplayName("GET download song for non premium returns 403")
    void downloadSong_nonPremium_returns403() throws Exception {
        when(downloadService.downloadSong(USER_ID, SONG_ID)).thenThrow(new AccessDeniedException("Premium subscription required to download songs"));

        mockMvc.perform(get(URL, SONG_ID)
                        .param("userId", String.valueOf(USER_ID))
                        .with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET download song without JWT returns 403")
    void downloadSong_noJwt_returns403() throws Exception {
        mockMvc.perform(get(URL, SONG_ID).param("userId", String.valueOf(USER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET download song for missing song returns 404")
    void downloadSong_songNotFound_returns404() throws Exception {
        when(downloadService.downloadSong(USER_ID, SONG_ID)).thenThrow(new ResourceNotFoundException("Song", SONG_ID));

        mockMvc.perform(get(URL, SONG_ID)
                        .param("userId", String.valueOf(USER_ID))
                        .with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isNotFound());
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(USER_ID, "listener", role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
