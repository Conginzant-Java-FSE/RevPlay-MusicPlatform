package com.revplay.revplay.service.impl;

import com.revplay.revplay.entity.PlayHistory;
import com.revplay.revplay.enums.TimePeriod;
import com.revplay.revplay.repository.PlayHistoryRepository;
import com.revplay.revplay.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private static final int SESSION_SIZE = 20;
    private static final double MIN_COMPLETION_RATE = 70.0;
    private static final int RECOMMENDATION_LIMIT = 10;

    private final PlayHistoryRepository historyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> getSimilarContent(Long contentId, String contentType, TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        Pageable limit = PageRequest.of(0, RECOMMENDATION_LIMIT);

        List<Object[]> results;

        if ("SONG".equalsIgnoreCase(contentType)) {
            results = historyRepository.findSongCoListening(contentId, startDate, limit);
        } else {
            results = historyRepository.findEpisodeCoListening(contentId, startDate, limit);
        }

        return results.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getPersonalizedRecommendations(Long userId) {

        Pageable sessionLimit = PageRequest.of(0, SESSION_SIZE);
        List<PlayHistory> recentSession =
                historyRepository.findUserRecentSession(userId, sessionLimit);

        if (recentSession.isEmpty()) {
            return getPopularHighCompletionContent();
        }

        Set<Long> sessionSongIds = recentSession.stream()
                .filter(ph -> ph.getSongId() != null)
                .map(PlayHistory::getSongId)
                .collect(Collectors.toSet());

        Set<Long> recommendations = new HashSet<>();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        for (Long songId : sessionSongIds.stream().limit(5).collect(Collectors.toList())) {
            List<Object[]> coListening = historyRepository.findSongCoListening(
                    songId, oneWeekAgo, PageRequest.of(0, 5)
            );

            coListening.stream()
                    .map(row -> (Long) row[0])
                    .forEach(recommendations::add);
        }

        recommendations.removeAll(sessionSongIds);

        return filterByCompletionRate(new ArrayList<>(recommendations));
    }

    private List<Long> filterByCompletionRate(List<Long> contentIds) {

        if (contentIds.isEmpty()) {
            return contentIds;
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        Pageable limit = PageRequest.of(0, 100);

        List<Object[]> highCompletionContent =
                historyRepository.findHighCompletionSongs(
                        oneMonthAgo, MIN_COMPLETION_RATE, limit
                );

        Set<Long> highQualityIds = highCompletionContent.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toSet());

        return contentIds.stream()
                .filter(highQualityIds::contains)
                .limit(RECOMMENDATION_LIMIT)
                .collect(Collectors.toList());
    }

    private List<Long> getPopularHighCompletionContent() {

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        Pageable limit = PageRequest.of(0, RECOMMENDATION_LIMIT);

        List<Object[]> results =
                historyRepository.findHighCompletionSongs(
                        oneMonthAgo, MIN_COMPLETION_RATE, limit
                );

        return results.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());
    }
}
