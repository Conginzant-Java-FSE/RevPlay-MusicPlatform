package com.revplay.revplay.service;

import com.revplay.revplay.enums.TimePeriod;

import java.util.List;

public interface RecommendationService {

    List<Long> getSimilarContent(Long contentId, String contentType, TimePeriod period);

    List<Long> getPersonalizedRecommendations(Long userId);
}
