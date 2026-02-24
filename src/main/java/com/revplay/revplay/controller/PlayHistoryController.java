package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.TrackPlayRequest;
import com.revplay.revplay.dto.response.PlayHistoryResponse;
import com.revplay.revplay.dto.response.RecentPlayResponse;
import com.revplay.revplay.service.PlayHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class PlayHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(PlayHistoryController.class);

    private final PlayHistoryService historyService;

    @PostMapping("/track")
    public ResponseEntity<PlayHistoryResponse> trackPlay(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TrackPlayRequest request) {

        Long userId = extractUserId(userDetails);
        logger.info("POST /api/history/track - userId: {}", userId);

        PlayHistoryResponse response = historyService.trackPlay(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PlayHistoryResponse>> getUserHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "playedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/history - userId: {}", userId);

        Page<PlayHistoryResponse> history = historyService.getUserHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentPlayResponse>> getRecentPlays(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/history/recent - userId: {}", userId);

        List<RecentPlayResponse> recentPlays = historyService.getRecentPlays(userId);
        return ResponseEntity.ok(recentPlays);
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }

    /**
     * Clear user's play history
     * PROJECT REQUIREMENT: Line 44 - "Clear listening history"
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        logger.info("DELETE /api/history/clear - userId: {}", userId);

        historyService.clearHistory(userId);
        return ResponseEntity.noContent().build();
    }

}

