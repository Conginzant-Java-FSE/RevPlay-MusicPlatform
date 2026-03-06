package com.revplay.musicplatform.playback.controller;

import com.revplay.musicplatform.catalog.dto.response.SongResponse;
import com.revplay.musicplatform.catalog.entity.Song;
import com.revplay.musicplatform.catalog.mapper.SongMapper;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.playback.service.AutoplayService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(AutoplayController.class)
@Import(SecurityConfig.class)
class AutoplayControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private AutoplayService autoplayService;
    @MockBean
    private SongMapper songMapper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenRevocationService tokenRevocationService;
    @MockBean
    private FileStorageProperties fileStorageProperties;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    AutoplayControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("GET /api/v1/autoplay/next/{userId}/{songId} authenticated returns 200")
    void getNext_authenticated_200() throws Exception {
        Song song = new Song();
        song.setSongId(10L);
        SongResponse dto = new SongResponse();
        dto.setSongId(10L);
        dto.setTitle("Next Song");
        when(autoplayService.getNextSong(1L, 9L)).thenReturn(song);
        when(songMapper.toResponse(song)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/autoplay/next/{userId}/{songId}", 1L, 9L).with(authUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.songId").value(10))
                .andExpect(jsonPath("$.data.title").value("Next Song"));

        verify(autoplayService).getNextSong(1L, 9L);
    }

    private RequestPostProcessor authUser() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(1L, "u1", UserRole.LISTENER);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
