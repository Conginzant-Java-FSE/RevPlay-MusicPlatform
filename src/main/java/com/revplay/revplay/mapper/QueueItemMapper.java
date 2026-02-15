package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.QueueItemResponse;
import com.revplay.revplay.entity.QueueItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueueItemMapper {

    public QueueItemResponse toResponse(QueueItem entity) {
        if (entity == null) {
            return null;
        }
        return new QueueItemResponse(
                entity.getQueueId(),
                entity.getUserId(),
                entity.getSongId(),
                entity.getEpisodeId(),
                entity.getPosition(),
                entity.getCreatedAt()
        );
    }

    public List<QueueItemResponse> toResponseList(List<QueueItem> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
