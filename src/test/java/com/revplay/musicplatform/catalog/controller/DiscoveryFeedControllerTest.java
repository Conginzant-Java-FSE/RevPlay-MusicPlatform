package com.revplay.musicplatform.catalog.controller;

import com.revplay.musicplatform.catalog.dto.response.DiscoverWeeklyResponse;
import com.revplay.musicplatform.catalog.dto.response.DiscoveryFeedResponse;
import com.revplay.musicplatform.catalog.service.DiscoveryFeedService;
import com.revplay.musicplatform.common.MockSecurityContextHelper;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(DiscoveryFeedController.class)

@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityContextHelper.class)
class DiscoveryFeedControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean

        private JpaMetamodelMappingContext jpaMetamodelMappingContext;


        @MockBean
        private FileStorageProperties fileStorageProperties;


        @MockBean
        private DiscoveryFeedService discoveryFeedService;

        @MockBean
        private JwtServiceImpl jwtService;

        @MockBean
        private TokenRevocationServiceImpl tokenRevocationService;

        @Autowired
        private MockSecurityContextHelper securityContextHelper;

        @Test
        @DisplayName("GET /api/v1/discover/weekly/{userId}: success")
        void discoverWeekly_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(discoveryFeedService.discoverWeekly(eq(1L), anyInt()))
                                .thenReturn(new DiscoverWeeklyResponse(1L, List.of()));

                mockMvc.perform(get("/api/v1/discover/weekly/1"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/discover/feed/{userId}: success")
        void homeFeed_Success() throws Exception {
                securityContextHelper.setMockUser(1L, "user", UserRole.LISTENER.name());

                when(discoveryFeedService.homeFeed(eq(1L), anyInt()))
                                .thenReturn(new DiscoveryFeedResponse(1L, List.of(), List.of(), List.of(), List.of()));

                mockMvc.perform(get("/api/v1/discover/feed/1"))
                                .andExpect(status().isOk());
        }
}



