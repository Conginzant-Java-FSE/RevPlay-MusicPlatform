package com.revplay.musicplatform.catalog.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.revplay.musicplatform.analytics.dto.response.ForYouRecommendationsResponse;
import com.revplay.musicplatform.analytics.dto.response.SongRecommendationResponse;
import com.revplay.musicplatform.analytics.service.RecommendationService;
import com.revplay.musicplatform.catalog.dto.response.DiscoverWeeklyResponse;
import com.revplay.musicplatform.catalog.dto.response.DiscoveryFeedResponse;
import com.revplay.musicplatform.catalog.dto.response.NewReleaseItemResponse;
import com.revplay.musicplatform.catalog.exception.DiscoveryValidationException;
import com.revplay.musicplatform.catalog.service.BrowseService;
import com.revplay.musicplatform.common.dto.PagedResponseDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class DiscoveryFeedServiceImplTest {

    private static final Long USER_ID = 11L;
    private static final int LIMIT = 10;

    @Mock
    private RecommendationService recommendationService;
    @Mock
    private BrowseService browseService;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DiscoveryFeedServiceImpl service;

    @Test
    @DisplayName("discoverWeekly throws when user id is invalid")
    void discoverWeeklyInvalidUserId() {
        assertThatThrownBy(() -> service.discoverWeekly(0L, LIMIT))
                .isInstanceOf(DiscoveryValidationException.class);
    }

    @Test
    @DisplayName("discoverWeekly throws when limit out of range")
    void discoverWeeklyInvalidLimit() {
        assertThatThrownBy(() -> service.discoverWeekly(USER_ID, 0))
                .isInstanceOf(DiscoveryValidationException.class)
                .hasMessage("limit must be between 1 and 100");
    }

    @Test
    @DisplayName("discoverWeekly uses recommendation fallback when recent signal unavailable")
    void discoverWeeklyFallbackFromRecommendationService() {
        when(jdbcTemplate.queryForObject(any(String.class), any(Class.class), any())).thenReturn(0L);
        when(recommendationService.forUser(USER_ID, LIMIT)).thenReturn(
                new ForYouRecommendationsResponse(
                        USER_ID,
                        List.of(new SongRecommendationResponse(1L, "S", 2L, "A", 99L)),
                        List.of()
                )
        );

        DiscoverWeeklyResponse response = service.discoverWeekly(USER_ID, LIMIT);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).songId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("homeFeed combines browse sections and discover weekly section")
    void homeFeedSuccess() {
        PagedResponseDto<NewReleaseItemResponse> newReleases = new PagedResponseDto<>(List.of(), 0, LIMIT, 0, 0, "releaseDate", "DESC");
        when(browseService.newReleases(0, LIMIT, "DESC")).thenReturn(newReleases);
        when(browseService.topArtists(0, LIMIT)).thenReturn(new PagedResponseDto<>(List.of(), 0, LIMIT, 0, 0, "playCount", "DESC"));
        when(browseService.popularPodcasts(0, LIMIT)).thenReturn(new PagedResponseDto<>(List.of(), 0, LIMIT, 0, 0, "playCount", "DESC"));
        when(jdbcTemplate.queryForObject(any(String.class), any(Class.class), any())).thenReturn(0L);
        when(recommendationService.forUser(anyLong(), anyInt())).thenReturn(new ForYouRecommendationsResponse(USER_ID, List.of(), List.of()));

        DiscoveryFeedResponse response = service.homeFeed(USER_ID, LIMIT);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.newReleases()).isEmpty();
        assertThat(response.discoverWeekly()).isEmpty();
    }

    @Test
    @DisplayName("homeFeed throws when sectionLimit out of range")
    void homeFeedInvalidSectionLimit() {
        assertThatThrownBy(() -> service.homeFeed(USER_ID, 0))
                .isInstanceOf(DiscoveryValidationException.class)
                .hasMessage("sectionLimit must be between 1 and 50");
    }
}
