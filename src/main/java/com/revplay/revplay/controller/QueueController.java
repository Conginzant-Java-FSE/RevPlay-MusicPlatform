package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.AddToQueueRequest;
import com.revplay.revplay.dto.request.ReorderQueueRequest;
import com.revplay.revplay.dto.response.QueueItemResponse;
import com.revplay.revplay.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private static final Logger logger = LoggerFactory.getLogger(QueueController.class);

    private final QueueService queueService;

    @PostMapping
    public ResponseEntity<QueueItemResponse> addToQueue(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToQueueRequest request) {

        Long userId = extractUserId(userDetails);
        logger.info("POST /api/queue - userId: {}", userId);

        QueueItemResponse response = queueService.addToQueue(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<QueueItemResponse>> getUserQueue(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/queue - userId: {}", userId);

        List<QueueItemResponse> queue = queueService.getUserQueue(userId);
        return ResponseEntity.ok(queue);
    }

    @DeleteMapping("/{queueId}")
    public ResponseEntity<Void> removeFromQueue(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long queueId) {

        Long userId = extractUserId(userDetails);
        logger.info("DELETE /api/queue/{} - userId: {}", queueId, userId);

        queueService.removeFromQueue(userId, queueId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<QueueItemResponse> reorderQueue(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReorderQueueRequest request) {

        Long userId = extractUserId(userDetails);
        logger.info("PUT /api/queue/reorder - userId: {}", userId);

        QueueItemResponse response = queueService.reorderQueue(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shuffle")
    public ResponseEntity<List<QueueItemResponse>> shuffleQueue(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        logger.info("POST /api/queue/shuffle - userId: {}", userId);

        List<QueueItemResponse> shuffled = queueService.shuffleQueue(userId);
        return ResponseEntity.ok(shuffled);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearQueue(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        logger.info("DELETE /api/queue/clear - userId: {}", userId);

        queueService.clearQueue(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/next")
    public ResponseEntity<QueueItemResponse> getNext(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/queue/next - userId: {}", userId);

        QueueItemResponse next = queueService.getNextInQueue(userId);
        return ResponseEntity.ok(next);
    }

    @GetMapping("/previous")
    public ResponseEntity<QueueItemResponse> getPrevious(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Integer currentPosition) {

        Long userId = extractUserId(userDetails);
        logger.info("GET /api/queue/previous - userId: {}", userId);

        QueueItemResponse previous = queueService.getPreviousInQueue(userId, currentPosition);
        return ResponseEntity.ok(previous);
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}

