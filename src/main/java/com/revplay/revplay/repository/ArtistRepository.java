package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Artist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

