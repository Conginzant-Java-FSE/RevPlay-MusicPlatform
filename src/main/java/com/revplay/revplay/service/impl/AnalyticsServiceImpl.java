package com.revplay.revplay.service.impl;

import com.revplay.revplay.dto.response.*;
import com.revplay.revplay.enums.TimePeriod;
import com.revplay.revplay.repository.PlayHistoryRepository;
import com.revplay.revplay.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int DEFAULT_TOP_LIMIT = 10;

    private final PlayHistoryRepository historyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TopContentResponse> getTopSongs(Long userId, TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        Pageable limit = PageRequest.of(0, DEFAULT_TOP_LIMIT);

        List<Object[]> results =
                historyRepository.findTopSongsByUserId(userId, startDate, limit);

        return results.stream()
                .map(row -> new TopContentResponse(
                        (Long) row[0],
                        "SONG",
                        (Long) row[1]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopContentResponse> getTopEpisodes(Long userId, TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        Pageable limit = PageRequest.of(0, DEFAULT_TOP_LIMIT);

        List<Object[]> results =
                historyRepository.findTopEpisodesByUserId(userId, startDate, limit);

        return results.stream()
                .map(row -> new TopContentResponse(
                        (Long) row[0],
                        "EPISODE",
                        (Long) row[1]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopContentResponse> getTrendingSongs(TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        Pageable limit = PageRequest.of(0, DEFAULT_TOP_LIMIT);

        List<Object[]> results =
                historyRepository.findTrendingSongs(startDate, limit);

        return results.stream()
                .map(row -> new TopContentResponse(
                        (Long) row[0],
                        "SONG",
                        (Long) row[1]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopContentResponse> getTrendingEpisodes(TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        Pageable limit = PageRequest.of(0, DEFAULT_TOP_LIMIT);

        List<Object[]> results =
                historyRepository.findTrendingEpisodes(startDate, limit);

        return results.stream()
                .map(row -> new TopContentResponse(
                        (Long) row[0],
                        "EPISODE",
                        (Long) row[1]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnhancedListeningStatsResponse getEnhancedListeningStats(Long userId,
                                                                    TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();

        long totalPlays = historyRepository.countPlaysInPeriod(startDate);
        long uniqueSongs = historyRepository.countUniqueSongsInPeriod(userId, startDate);
        long uniqueEpisodes = historyRepository.countUniqueEpisodesInPeriod(userId, startDate);

        Map<Integer, Long> peakHours = calculatePeakHours(userId, startDate);

        Double completionRate =
                historyRepository.findUserCompletionRate(userId, startDate);

        Long totalListenTime =
                historyRepository.findUserTotalListenTime(userId, startDate);

        EnhancedListeningStatsResponse.ContentTypeBreakdown breakdown =
                calculateContentTypeBreakdown(userId, startDate);

        EnhancedListeningStatsResponse stats =
                new EnhancedListeningStatsResponse();

        stats.setTotalPlays(totalPlays);
        stats.setUniqueSongs(uniqueSongs);
        stats.setUniqueEpisodes(uniqueEpisodes);
        stats.setPeakHours(peakHours);
        stats.setCompletionRate(completionRate != null ? completionRate : 0.0);
        stats.setTotalListenTimeSeconds(totalListenTime != null ? totalListenTime : 0L);
        stats.setAvgDurationSeconds(
                totalPlays > 0 ? (int) (totalListenTime / totalPlays) : 0
        );
        stats.setBreakdown(breakdown);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public CompletionMetricsResponse getSongCompletionMetrics(Long songId,
                                                              TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        Object[] metrics =
                historyRepository.findSongCompletionMetrics(songId, startDate);

        Long totalPlays = ((Number) metrics[0]).longValue();
        Long completedPlays = ((Number) metrics[1]).longValue();

        Double completionRate =
                totalPlays > 0
                        ? (completedPlays.doubleValue() / totalPlays) * 100
                        : 0.0;

        return new CompletionMetricsResponse(
                songId,
                "SONG",
                totalPlays,
                completedPlays,
                completionRate,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContentComparisonResponse compareContentTypes(TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        List<Object[]> results =
                historyRepository.findPlatformContentComparison(startDate);

        ContentComparisonResponse.ContentTypeMetrics songs = null;
        ContentComparisonResponse.ContentTypeMetrics podcasts = null;

        for (Object[] row : results) {

            String contentType = (String) row[0];
            Long totalPlays = ((Number) row[1]).longValue();
            Long uniqueListeners = ((Number) row[2]).longValue();
            Integer avgDuration =
                    row[3] != null ? ((Number) row[3]).intValue() : 0;
            Double completionRate =
                    row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

            ContentComparisonResponse.ContentTypeMetrics metrics =
                    new ContentComparisonResponse.ContentTypeMetrics(
                            totalPlays,
                            uniqueListeners,
                            avgDuration,
                            completionRate
                    );

            if ("SONG".equals(contentType)) {
                songs = metrics;
            } else {
                podcasts = metrics;
            }
        }

        if (songs == null)
            songs = new ContentComparisonResponse.ContentTypeMetrics(0L, 0L, 0, 0.0);

        if (podcasts == null)
            podcasts = new ContentComparisonResponse.ContentTypeMetrics(0L, 0L, 0, 0.0);

        return new ContentComparisonResponse(songs, podcasts);
    }

    @Override
    @Transactional(readOnly = true)
    public EngagementTrendResponse getEngagementTrends(TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();
        List<Object[]> results;

        switch (period) {
            case DAILY:
                results = historyRepository.findDailyEngagementTrends(startDate);
                break;
            case WEEKLY:
                results = historyRepository.findWeeklyEngagementTrends(startDate);
                break;
            case MONTHLY:
                results = historyRepository.findMonthlyEngagementTrends(startDate);
                break;
            default:
                results = historyRepository.findWeeklyEngagementTrends(startDate);
        }

        List<EngagementTrendResponse.DataPoint> dataPoints =
                results.stream()
                        .map(row -> {
                            LocalDate date =
                                    row[0] instanceof LocalDate
                                            ? (LocalDate) row[0]
                                            : LocalDate.now();

                            Long playCount =
                                    ((Number) row[1]).longValue();

                            Long uniqueListeners =
                                    ((Number) row[2]).longValue();

                            Double avgCompletionRate =
                                    row[3] != null
                                            ? ((Number) row[3]).doubleValue()
                                            : 0.0;

                            return new EngagementTrendResponse.DataPoint(
                                    date,
                                    playCount,
                                    uniqueListeners,
                                    avgCompletionRate
                            );
                        })
                        .collect(Collectors.toList());

        return new EngagementTrendResponse(period.name(), dataPoints);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getDashboardAnalytics(TimePeriod period) {

        LocalDateTime startDate = period.getStartDate();

        long totalPlays = historyRepository.countPlaysInPeriod(startDate);
        long uniqueListeners = historyRepository.countActiveUsers(startDate);
        Double avgCompletionRate =
                historyRepository.findPlatformCompletionRate(startDate);

        Double avgSongDuration =
                historyRepository.findAvgSongDuration(startDate);

        Double avgEpisodeDuration =
                historyRepository.findAvgEpisodeDuration(startDate);

        int avgDuration =
                (int) (((avgSongDuration != null ? avgSongDuration : 0.0)
                        + (avgEpisodeDuration != null ? avgEpisodeDuration : 0.0)) / 2);

        DashboardAnalyticsResponse.OverviewMetrics overview =
                new DashboardAnalyticsResponse.OverviewMetrics(
                        totalPlays,
                        uniqueListeners,
                        avgCompletionRate != null ? avgCompletionRate : 0.0,
                        avgDuration
                );

        ContentComparisonResponse comparison =
                compareContentTypes(period);

        List<Object[]> peakHoursData =
                historyRepository.findPlatformPeakHours(startDate);

        Map<Integer, Long> peakHours = new HashMap<>();
        for (Object[] row : peakHoursData) {
            peakHours.put((Integer) row[0], ((Number) row[1]).longValue());
        }

        List<TopContentResponse> topSongs =
                getTrendingSongs(period);

        List<TopContentResponse> topPodcasts =
                getTrendingEpisodes(period);

        DashboardAnalyticsResponse dashboard =
                new DashboardAnalyticsResponse();

        dashboard.setOverview(overview);
        dashboard.setSongs(comparison.getSongs());
        dashboard.setPodcasts(comparison.getPodcasts());
        dashboard.setPeakHours(peakHours);
        dashboard.setTopCreators(new ArrayList<>());
        dashboard.setTopSongs(topSongs);
        dashboard.setTopPodcasts(topPodcasts);

        return dashboard;
    }

    private Map<Integer, Long> calculatePeakHours(Long userId,
                                                  LocalDateTime startDate) {

        List<Object[]> results =
                historyRepository.findListeningHoursByUserId(userId, startDate);

        Map<Integer, Long> peakHours = new HashMap<>();

        for (Object[] row : results) {
            peakHours.put((Integer) row[0],
                    ((Number) row[1]).longValue());
        }

        return peakHours;
    }

    private EnhancedListeningStatsResponse.ContentTypeBreakdown
    calculateContentTypeBreakdown(Long userId,
                                  LocalDateTime startDate) {

        List<Object[]> results =
                historyRepository.findContentComparisonByUser(userId, startDate);

        long songPlays = 0;
        long podcastPlays = 0;
        double songCompletionRate = 0.0;
        double podcastCompletionRate = 0.0;

        for (Object[] row : results) {

            String contentType = (String) row[0];
            Long plays = ((Number) row[1]).longValue();
            Double completionRate =
                    row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

            if ("SONG".equals(contentType)) {
                songPlays = plays;
                songCompletionRate = completionRate;
            } else {
                podcastPlays = plays;
                podcastCompletionRate = completionRate;
            }
        }

        return new EnhancedListeningStatsResponse.ContentTypeBreakdown(
                songPlays,
                podcastPlays,
                songCompletionRate,
                podcastCompletionRate
        );
    }
}