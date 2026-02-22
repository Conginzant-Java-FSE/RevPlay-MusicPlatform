package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.PlayHistoryResponse;
import com.revplay.revplay.dto.response.RecentPlayResponse;
import com.revplay.revplay.entity.PlayHistory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlayHistoryMapper {

    public PlayHistoryResponse toResponse(PlayHistory entity) {
        if (entity == null) {
            return null;
        }
        return new PlayHistoryResponse(
                entity.getPlayId(),
                entity.getUserId(),
                entity.getSongId(),
                entity.getEpisodeId(),
                entity.getPlayedAt(),
                entity.getCompleted()
        );
    }

    public List<PlayHistoryResponse> toResponseList(List<PlayHistory> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<PlayHistoryResponse> toResponsePage(Page<PlayHistory> page) {
        return page.map(this::toResponse);
    }

    public RecentPlayResponse toRecentPlayResponse(PlayHistory entity) {
        if (entity == null) {
            return null;
        }
        String contentType = entity.getSongId() != null ? "SONG" : "EPISODE";
        Long contentId = entity.getSongId() != null ? entity.getSongId() : entity.getEpisodeId();

        return new RecentPlayResponse(contentId, contentType, entity.getPlayedAt());
    }

    public List<RecentPlayResponse> toRecentPlayResponseList(List<PlayHistory> entities) {
        return entities.stream()
                .map(this::toRecentPlayResponse)
                .collect(Collectors.toList());
    }
}

