package com.revplay.revplay.service;



import com.revplay.revplay.dto.request.PodcastCreateRequest;
import com.revplay.revplay.dto.request.PodcastUpdateRequest;
import com.revplay.revplay.dto.response.PodcastResponse;

import java.util.List;

public interface PodcastService {

    PodcastResponse createPodcast(PodcastCreateRequest request);

    PodcastResponse updatePodcast(Long podcastId, PodcastUpdateRequest request);

    PodcastResponse getPodcastById(Long podcastId);

    List<PodcastResponse> getPodcastsByArtist(Long artistId);

    void deletePodcast(Long podcastId);
}
