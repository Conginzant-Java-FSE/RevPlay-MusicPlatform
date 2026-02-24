package com.revplay.revplay.controller;

import com.revplay.revplay.enums.TimePeriod;
import com.revplay.revplay.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    @GetMapping("/similar/{contentId}")
    public ResponseEntity<List<Long>> getSimilarContent(
            @PathVariable Long contentId,
            @RequestParam String contentType,
            @RequestParam(defaultValue = "WEEKLY") TimePeriod period) {

        logger.info("GET /api/recommendations/similar/{} - type: {}, period: {}",
                contentId, contentType, period);

        List<Long> similar = recommendationService.getSimilarContent(contentId, contentType, period);
        return ResponseEntity.ok(similar);
    }

    @GetMapping("/for-you")
    public ResponseEntity<List<Long>> getPersonalizedRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/recommendations/for-you - userId: {}", userId);

        List<Long> recommendations = recommendationService.getPersonalizedRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
