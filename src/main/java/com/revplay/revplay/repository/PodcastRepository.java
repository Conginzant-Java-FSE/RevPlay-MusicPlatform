package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Podcast;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    List<Podcast> findByArtist_ArtistId(Long artistId);
    List<Podcast> findByCategory_CategoryId(Long categoryId);
}
