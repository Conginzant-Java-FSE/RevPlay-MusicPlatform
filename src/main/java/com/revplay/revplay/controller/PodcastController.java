package com.revplay.revplay.controller;




import com.revplay.revplay.dto.request.PodcastCreateRequest;
import com.revplay.revplay.dto.request.PodcastUpdateRequest;
import com.revplay.revplay.dto.response.PodcastResponse;
import com.revplay.revplay.service.PodcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/podcasts")
@RequiredArgsConstructor
public class PodcastController {

    private final PodcastService podcastService;

    @PostMapping
    public ResponseEntity<PodcastResponse> create(
            @Valid @RequestBody PodcastCreateRequest request) {
        return ResponseEntity.ok(podcastService.createPodcast(request));
    }

    @PutMapping("/{podcastId}")
    public ResponseEntity<PodcastResponse> update(
            @PathVariable Long podcastId,
            @Valid @RequestBody PodcastUpdateRequest request) {
        return ResponseEntity.ok(podcastService.updatePodcast(podcastId, request));
    }

    @GetMapping("/{podcastId}")
    public ResponseEntity<PodcastResponse> getById(@PathVariable Long podcastId) {
        return ResponseEntity.ok(podcastService.getPodcastById(podcastId));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<PodcastResponse>> getByArtist(
            @PathVariable Long artistId) {
        return ResponseEntity.ok(podcastService.getPodcastsByArtist(artistId));
    }

    @DeleteMapping("/{podcastId}")
    public ResponseEntity<Void> delete(@PathVariable Long podcastId) {
        podcastService.deletePodcast(podcastId);
        return ResponseEntity.noContent().build();
    }
}
