package com.revplay.revplay.service.Impl;

import com.revplay.revplay.dto.request.AddToQueueRequest;
import com.revplay.revplay.dto.request.ReorderQueueRequest;
import com.revplay.revplay.dto.response.QueueItemResponse;
import com.revplay.revplay.entity.QueueItem;
import com.revplay.revplay.exception.BadRequestException;
import com.revplay.revplay.exception.EmptyQueueException;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.exception.UnauthorizedException;
import com.revplay.revplay.mapper.QueueItemMapper;
import com.revplay.revplay.repository.QueueItemRepository;
import com.revplay.revplay.service.QueueService;
import com.revplay.revplay.util.PlaybackConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueServiceImpl.class);

    private final QueueItemRepository queueRepository;
    private final QueueItemMapper queueMapper;

    @Override
    @Transactional
    public QueueItemResponse addToQueue(Long userId, AddToQueueRequest request) {
        request.validate();

        long currentSize = queueRepository.countByUserId(userId);
        if (currentSize >= PlaybackConstants.MAX_QUEUE_SIZE) {
            throw new BadRequestException(
                    String.format("Queue size limit reached (%d items)", PlaybackConstants.MAX_QUEUE_SIZE));
        }

        Integer maxPosition = queueRepository.findMaxPositionByUserId(userId);
        int newPosition = (maxPosition == null) ? 1 : maxPosition + 1;

        QueueItem queueItem = new QueueItem();
        queueItem.setUserId(userId);
        queueItem.setSongId(request.getSongId());
        queueItem.setEpisodeId(request.getEpisodeId());
        queueItem.setPosition(newPosition);
        queueItem.setCreatedAt(LocalDateTime.now());

        QueueItem saved = queueRepository.save(queueItem);

        logger.info("Added to queue: userId={}, contentId={}, position={}",
                userId, getContentId(saved), newPosition);

        return queueMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueItemResponse> getUserQueue(Long userId) {
        List<QueueItem> queueItems =
                queueRepository.findByUserIdOrderByPositionAsc(userId);

        logger.debug("Retrieved queue for userId={}: {} items",
                userId, queueItems.size());

        return queueMapper.toResponseList(queueItems);
    }

    @Override
    @Transactional
    public void removeFromQueue(Long userId, Long queueId) {

        QueueItem queueItem = queueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue item not found with id: " + queueId));

        validateQueueOwnership(userId, queueItem);

        int removedPosition = queueItem.getPosition();

        queueRepository.deleteById(queueId);

        queueRepository.decrementPositionsAfter(userId, removedPosition);

        logger.info("Removed from queue: userId={}, queueId={}, position={}",
                userId, queueId, removedPosition);
    }

    @Override
    @Transactional
    public QueueItemResponse reorderQueue(Long userId, ReorderQueueRequest request) {

        QueueItem queueItem = queueRepository.findById(request.getQueueId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue item not found with id: " + request.getQueueId()));

        validateQueueOwnership(userId, queueItem);

        int currentPosition = queueItem.getPosition();
        int newPosition = request.getNewPosition();

        if (newPosition <= 0) {
            throw new BadRequestException("Position must be greater than 0");
        }

        if (currentPosition == newPosition) {
            return queueMapper.toResponse(queueItem);
        }

        long queueSize = queueRepository.countByUserId(userId);
        if (newPosition > queueSize) {
            throw new BadRequestException(
                    String.format("Invalid position: %d (queue size: %d)",
                            newPosition, queueSize));
        }

        if (Math.abs(newPosition - currentPosition)
                > PlaybackConstants.MAX_POSITION_SHIFT) {
            throw new BadRequestException("Position shift too large");
        }

        if (newPosition < currentPosition) {
            queueRepository.shiftPositionsForReorder(
                    userId, newPosition, currentPosition);
        } else {
            queueRepository.shiftPositionsForReorder(
                    userId, currentPosition + 1, newPosition + 1);
        }

        queueItem.setPosition(newPosition);
        QueueItem updated = queueRepository.save(queueItem);

        logger.info("Reordered queue item: userId={}, queueId={}, from={}, to={}",
                userId, request.getQueueId(), currentPosition, newPosition);

        return queueMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public List<QueueItemResponse> shuffleQueue(Long userId) {

        List<QueueItem> queueItems =
                queueRepository.findByUserIdOrderByPositionAsc(userId);

        if (queueItems.isEmpty()) {
            throw new EmptyQueueException("Cannot shuffle empty queue");
        }

        Collections.shuffle(queueItems);

        for (int i = 0; i < queueItems.size(); i++) {
            queueItems.get(i).setPosition(i + 1);
        }

        List<QueueItem> shuffled = queueRepository.saveAll(queueItems);

        logger.info("Shuffled queue for userId={}: {} items",
                userId, shuffled.size());

        return queueMapper.toResponseList(shuffled);
    }

    @Override
    @Transactional
    public void clearQueue(Long userId) {
        queueRepository.deleteAllByUserId(userId);
        logger.info("Cleared queue for userId={}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public QueueItemResponse getNextInQueue(Long userId) {

        QueueItem next = queueRepository
                .findFirstByUserIdOrderByPositionAsc(userId)
                .orElseThrow(() -> new EmptyQueueException("Queue is empty"));

        return queueMapper.toResponse(next);
    }

    @Override
    @Transactional(readOnly = true)
    public QueueItemResponse getPreviousInQueue(Long userId,
                                                Integer currentPosition) {

        if (currentPosition <= 1) {
            throw new BadRequestException(
                    "Already at the beginning of queue");
        }

        QueueItem previous = queueRepository
                .findByUserIdAndPosition(userId, currentPosition - 1)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Previous item not found"));

        return queueMapper.toResponse(previous);
    }

    private void validateQueueOwnership(Long userId, QueueItem queueItem) {
        if (!queueItem.getUserId().equals(userId)) {
            throw new UnauthorizedException(
                    "User does not have access to this queue item");
        }
    }

    private Long getContentId(QueueItem item) {
        return item.getSongId() != null ?
                item.getSongId() : item.getEpisodeId();
    }
}

