package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.TrackPlayRequest;
import com.revplay.revplay.dto.response.PlayHistoryResponse;
import com.revplay.revplay.dto.response.RecentPlayResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlayHistoryService {

    PlayHistoryResponse trackPlay(Long userId, TrackPlayRequest request);

    Page<PlayHistoryResponse> getUserHistory(Long userId, Pageable pageable);

    List<RecentPlayResponse> getRecentPlays(Long userId);

    void clearHistory(Long userId);
}