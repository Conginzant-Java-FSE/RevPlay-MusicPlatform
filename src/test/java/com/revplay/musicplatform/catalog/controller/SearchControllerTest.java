package com.revplay.musicplatform.catalog.controller;

import com.revplay.musicplatform.catalog.dto.request.SearchRequest;
import com.revplay.musicplatform.catalog.dto.response.SearchResultItemResponse;
import com.revplay.musicplatform.catalog.service.SearchService;
import com.revplay.musicplatform.common.MockSecurityContextHelper;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import com.revplay.musicplatform.config.FileStorageProperties;
import com.revplay.musicplatform.playlist.service.PlaylistSearchService;
import com.revplay.musicplatform.security.service.DiscoveryRateLimiterService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(SearchController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class SearchControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean

        private JpaMetamodelMappingContext jpaMetamodelMappingContext;


        @MockBean
        private FileStorageProperties fileStorageProperties;


        @MockBean
        private SearchService searchService;

        @MockBean
        private PlaylistSearchService playlistSearchService;

        @MockBean
        private DiscoveryRateLimiterService discoveryRateLimiterService;

        @MockBean
        private JwtServiceImpl jwtService;

        @MockBean
        private TokenRevocationServiceImpl tokenRevocationService;

        @Autowired
        private MockSecurityContextHelper securityContextHelper;

        @Test
        @DisplayName("GET /api/v1/search: success")
        void search_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                PagedResponseDto<SearchResultItemResponse> mockResponse = new PagedResponseDto<>(
                                List.of(new SearchResultItemResponse("song", 1L, "Faded", 10L, "Alan Walker", "MUSIC",
                                                null)),
                                0, 20, 1L, 1, "releaseDate", "DESC");

                when(searchService.search(any(SearchRequest.class))).thenReturn(mockResponse);

                mockMvc.perform(get("/api/v1/search")
                                .param("q", "faded")
                                .param("type", "song"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.content[0].title").value("Faded"));

                verify(discoveryRateLimiterService).ensureWithinLimit(anyString(), anyInt(), anyInt(), anyString());
        }

        @Test
        @DisplayName("GET /api/v1/search/playlists: success")
        void searchPlaylists_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(playlistSearchService.searchPublicPlaylists(eq("gym"), anyInt(), anyInt()))
                                .thenReturn(new PagedResponseDto<>(List.of(), 0, 20, 0, 0, null, null));

                mockMvc.perform(get("/api/v1/search/playlists")
                                .param("keyword", "gym"))
                                .andExpect(status().isOk());
        }
}




