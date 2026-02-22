package com.revplay.revplay.service;

import com.revplay.revplay.entity.UserStatistics;

public interface UserStatisticsService {

    UserStatistics getUserStatistics(Long userId);

    UserStatistics calculateAndSaveStatistics(Long userId);

    void recalculateAllStatistics();
}
