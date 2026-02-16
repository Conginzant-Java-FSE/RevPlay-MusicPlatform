package com.revplay.revplay.repository;

import com.revplay.revplay.entity.QueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueItemRepository extends JpaRepository<QueueItem, Long> {

    List<QueueItem> findByUserIdOrderByPositionAsc(Long userId);

    Optional<QueueItem> findByUserIdAndPosition(Long userId, Integer position);

    Optional<QueueItem> findFirstByUserIdOrderByPositionAsc(Long userId);

    @Query("SELECT MAX(q.position) FROM QueueItem q WHERE q.userId = :userId")
    Integer findMaxPositionByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(q) FROM QueueItem q WHERE q.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM QueueItem q WHERE q.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE QueueItem q SET q.position = q.position - 1 " +
            "WHERE q.userId = :userId AND q.position > :position")
    void decrementPositionsAfter(@Param("userId") Long userId, @Param("position") Integer position);

    @Modifying
    @Query("UPDATE QueueItem q SET q.position = q.position + 1 " +
            "WHERE q.userId = :userId AND q.position >= :fromPos AND q.position < :toPos")
    void shiftPositionsForReorder(@Param("userId") Long userId,
                                  @Param("fromPos") Integer fromPos,
                                  @Param("toPos") Integer toPos);
}

