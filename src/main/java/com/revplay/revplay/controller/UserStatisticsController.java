package com.revplay.revplay.controller;

import com.revplay.revplay.entity.UserStatistics;
import com.revplay.revplay.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(UserStatisticsController.class);

    private final UserStatisticsService userStatisticsService;

    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserStatistics> getUserStatistics(@PathVariable Long userId) {
        logger.info("GET /api/users/{}/statistics", userId);
        UserStatistics statistics = userStatisticsService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
}
