package com.revplay.musicplatform.playback.repository;

import com.revplay.musicplatform.playback.entity.QueueItemEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueItemRepository extends JpaRepository<QueueItemEntity, Long> {

    List<QueueItemEntity> findByUserIdOrderByPositionAscQueueIdAsc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT queueItem
            FROM QueueItemEntity queueItem
            WHERE queueItem.userId = :userId
            ORDER BY queueItem.position ASC, queueItem.queueId ASC
            """)
    List<QueueItemEntity> findByUserIdForUpdate(@Param("userId") Long userId);

    Optional<QueueItemEntity> findTopByUserIdOrderByPositionDesc(Long userId);
}



