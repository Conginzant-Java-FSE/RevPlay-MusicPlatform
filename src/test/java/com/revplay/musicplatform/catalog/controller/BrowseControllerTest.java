package com.revplay.musicplatform.catalog.controller;

import com.revplay.musicplatform.catalog.service.BrowseService;
import com.revplay.musicplatform.common.MockSecurityContextHelper;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.security.service.impl.JwtServiceImpl;
import com.revplay.musicplatform.security.service.impl.TokenRevocationServiceImpl;
import com.revplay.musicplatform.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(BrowseController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class BrowseControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean

        private JpaMetamodelMappingContext jpaMetamodelMappingContext;


        @MockBean
        private FileStorageProperties fileStorageProperties;


        @MockBean
        private BrowseService browseService;

        @MockBean
        private JwtServiceImpl jwtService;

        @MockBean
        private TokenRevocationServiceImpl tokenRevocationService;

        @Autowired
        private MockSecurityContextHelper securityContextHelper;

        @Test
        @DisplayName("GET /api/v1/browse/new-releases: success")
        void newReleases_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(browseService.newReleases(anyInt(), anyInt(), anyString()))
                                .thenReturn(new PagedResponseDto<>(List.of(), 0, 20, 0, 0, null, null));

                mockMvc.perform(get("/api/v1/browse/new-releases"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/browse/top-artists: success")
        void topArtists_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(browseService.topArtists(anyInt(), anyInt()))
                                .thenReturn(new PagedResponseDto<>(List.of(), 0, 20, 0, 0, null, null));

                mockMvc.perform(get("/api/v1/browse/top-artists"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/browse/popular-podcasts: success")
        void popularPodcasts_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(browseService.popularPodcasts(anyInt(), anyInt()))
                                .thenReturn(new PagedResponseDto<>(List.of(), 0, 20, 0, 0, null, null));

                mockMvc.perform(get("/api/v1/browse/popular-podcasts"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/browse/songs: success")
        void allSongs_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(browseService.allSongs(anyInt(), anyInt(), anyString(), anyString()))
                                .thenReturn(new PagedResponseDto<>(List.of(), 0, 20, 0, 0, null, null));

                mockMvc.perform(get("/api/v1/browse/songs"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/browse/genres/{id}/songs: success")
        void songsByGenre_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(browseService.songsByGenre(eq(1L), anyInt(), anyInt(), anyString(), anyString()))
                                .thenReturn(new PagedResponseDto<>(List.of(), 0, 20, 0, 0, null, null));

                mockMvc.perform(get("/api/v1/browse/genres/1/songs"))
                                .andExpect(status().isOk());
        }
}



