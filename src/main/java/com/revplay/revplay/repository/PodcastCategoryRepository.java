package com.revplay.revplay.repository;

import com.revplay.revplay.entity.PodcastCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PodcastCategoryRepository extends JpaRepository<PodcastCategory, Long> {
    Optional<PodcastCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
