package com.revplay.revplay.repository;

import com.revplay.revplay.entity.PodcastEpisode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PodcastEpisodeRepository extends JpaRepository<PodcastEpisode, Long> {
    List<PodcastEpisode> findByPodcast_PodcastId(Long podcastId);
}
