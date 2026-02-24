package com.revplay.revplay.controller;

import com.revplay.revplay.dto.response.*;
import com.revplay.revplay.enums.TimePeriod;
import com.revplay.revplay.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    @GetMapping("/top-songs")
    public ResponseEntity<List<TopContentResponse>> getTopSongs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/analytics/top-songs - userId: {}, period: {}", userId, period);

        List<TopContentResponse> topSongs =
                analyticsService.getTopSongs(userId, period);

        return ResponseEntity.ok(topSongs);
    }

    @GetMapping("/top-episodes")
    public ResponseEntity<List<TopContentResponse>> getTopEpisodes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/analytics/top-episodes - userId: {}, period: {}", userId, period);

        List<TopContentResponse> topEpisodes =
                analyticsService.getTopEpisodes(userId, period);

        return ResponseEntity.ok(topEpisodes);
    }

    @GetMapping("/trending/songs")
    public ResponseEntity<List<TopContentResponse>> getTrendingSongs(
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        logger.info("GET /api/analytics/trending/songs - period: {}", period);

        List<TopContentResponse> trending =
                analyticsService.getTrendingSongs(period);

        return ResponseEntity.ok(trending);
    }

    @GetMapping("/trending/episodes")
    public ResponseEntity<List<TopContentResponse>> getTrendingEpisodes(
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        logger.info("GET /api/analytics/trending/episodes - period: {}", period);

        List<TopContentResponse> trending =
                analyticsService.getTrendingEpisodes(period);

        return ResponseEntity.ok(trending);
    }

    @GetMapping("/stats")
    public ResponseEntity<EnhancedListeningStatsResponse> getListeningStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/analytics/stats - userId: {}, period: {}", userId, period);

        EnhancedListeningStatsResponse stats =
                analyticsService.getEnhancedListeningStats(userId, period);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/completion/{songId}")
    public ResponseEntity<CompletionMetricsResponse> getSongCompletionMetrics(
            @PathVariable Long songId,
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        logger.info("GET /api/analytics/completion/{} - period: {}", songId, period);

        CompletionMetricsResponse metrics =
                analyticsService.getSongCompletionMetrics(songId, period);

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/content-comparison")
    public ResponseEntity<ContentComparisonResponse> compareContentTypes(
            @RequestParam(defaultValue = "MONTHLY") TimePeriod period) {

        logger.info("GET /api/analytics/content-comparison - period: {}", period);

        ContentComparisonResponse comparison =
                analyticsService.compareContentTypes(period);

        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/engagement-trends")
    public ResponseEntity<EngagementTrendResponse> getEngagementTrends(
            @RequestParam(defaultValue = "MONTHLY") TimePeriod period) {

        logger.info("GET /api/analytics/engagement-trends - period: {}", period);

        EngagementTrendResponse trends =
                analyticsService.getEngagementTrends(period);

        return ResponseEntity.ok(trends);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ARTIST')")
    public ResponseEntity<DashboardAnalyticsResponse> getDashboard(
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        logger.info("GET /api/analytics/dashboard - period: {}", period);

        DashboardAnalyticsResponse dashboard =
                analyticsService.getDashboardAnalytics(period);

        return ResponseEntity.ok(dashboard);
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
