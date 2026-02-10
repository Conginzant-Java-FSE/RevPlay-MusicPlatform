package com.revplay.revplay.repository;

import com.revplay.revplay.entity.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    Optional<UserStatistics> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT us.userId FROM UserStatistics us")
    java.util.List<Long> findAllUserIds();
}