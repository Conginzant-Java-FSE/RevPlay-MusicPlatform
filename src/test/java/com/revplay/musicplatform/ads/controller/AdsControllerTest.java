package com.revplay.musicplatform.ads.controller;

import com.revplay.musicplatform.ads.entity.Ad;
import com.revplay.musicplatform.ads.service.AdService;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AdsController.class)
@Import(SecurityConfig.class)
class AdsControllerTest {

    private static final Long USER_ID = 10L;
    private static final Long SONG_ID = 20L;
    private static final String NEXT_URL = "/api/v1/ads/next";

    private final MockMvc mockMvc;

    @MockBean
    private AdService adService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    AdsControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET next ad authenticated with no ad returns 200 and null data")
    void getNext_noAd_okNullData() throws Exception {
        when(adService.getNextAd(USER_ID, SONG_ID)).thenReturn(null);

        mockMvc.perform(get(NEXT_URL)
                        .param("userId", String.valueOf(USER_ID))
                        .param("songId", String.valueOf(SONG_ID))
                        .with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("GET next ad authenticated with ad available returns 200 and ad data")
    void getNext_withAd_okDataPresent() throws Exception {
        Ad ad = new Ad();
        ad.setId(9L);
        ad.setTitle("Sponsored Track");
        ad.setDurationSeconds(15);
        ad.setMediaUrl("/uploads/ads/x.mp3");
        ad.setIsActive(true);
        when(adService.getNextAd(USER_ID, SONG_ID)).thenReturn(ad);

        mockMvc.perform(get(NEXT_URL)
                        .param("userId", String.valueOf(USER_ID))
                        .param("songId", String.valueOf(SONG_ID))
                        .with(authentication(auth(UserRole.LISTENER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.title").value("Sponsored Track"));
    }

    @Test
    @DisplayName("GET next ad without JWT returns 403")
    void getNext_noJwt_forbidden() throws Exception {
        mockMvc.perform(get(NEXT_URL)
                        .param("userId", String.valueOf(USER_ID))
                        .param("songId", String.valueOf(SONG_ID)))
                .andExpect(status().isForbidden());
    }

    private UsernamePasswordAuthenticationToken auth(UserRole role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(USER_ID, "user", role);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
