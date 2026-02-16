package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.AddToQueueRequest;
import com.revplay.revplay.dto.request.ReorderQueueRequest;
import com.revplay.revplay.dto.response.QueueItemResponse;

import java.util.List;

public interface QueueService {

    QueueItemResponse addToQueue(Long userId, AddToQueueRequest request);

    List<QueueItemResponse> getUserQueue(Long userId);

    void removeFromQueue(Long userId, Long queueId);

    QueueItemResponse reorderQueue(Long userId, ReorderQueueRequest request);

    List<QueueItemResponse> shuffleQueue(Long userId);

    void clearQueue(Long userId);

    QueueItemResponse getNextInQueue(Long userId);

    QueueItemResponse getPreviousInQueue(Long userId, Integer currentPosition);
}
