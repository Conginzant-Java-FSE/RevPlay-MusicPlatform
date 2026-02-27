package com.revplay.musicplatform.playback.repository;

import com.revplay.musicplatform.playback.entity.PlayHistoryEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayHistoryRepository extends JpaRepository<PlayHistoryEntity, Long> {

    List<PlayHistoryEntity> findByUserIdOrderByPlayedAtDescPlayIdDesc(Long userId);

    List<PlayHistoryEntity> findByUserIdOrderByPlayedAtDescPlayIdDesc(Long userId, Pageable pageable);

    long deleteByUserId(Long userId);
}



