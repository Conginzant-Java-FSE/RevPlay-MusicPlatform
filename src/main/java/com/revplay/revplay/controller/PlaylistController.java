package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.PlaylistCreateRequest;
import com.revplay.revplay.dto.request.PlaylistUpdateRequest;
import com.revplay.revplay.dto.response.PlaylistResponse;
import com.revplay.revplay.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/{userId}")
    public PlaylistResponse createPlaylist(@PathVariable Long userId,
                                           @Valid @RequestBody PlaylistCreateRequest request) {
        return playlistService.createPlaylist(userId, request);
    }

    @PutMapping("/{playlistId}")
    public PlaylistResponse updatePlaylist(@PathVariable Long playlistId,
                                           @Valid @RequestBody PlaylistUpdateRequest request) {
        return playlistService.updatePlaylist(playlistId, request);
    }

    @GetMapping("/{playlistId}")
    public PlaylistResponse getPlaylistById(@PathVariable Long playlistId) {
        return playlistService.getPlaylistById(playlistId);
    }

    @GetMapping("/user/{userId}")
    public List<PlaylistResponse> getUserPlaylists(@PathVariable Long userId) {
        return playlistService.getPlaylistsByUser(userId);
    }

    @GetMapping("/public")
    public List<PlaylistResponse> getPublicPlaylists() {
        return playlistService.getPublicPlaylists();
    }

    @DeleteMapping("/{playlistId}")
    public void deletePlaylist(@PathVariable Long playlistId) {
        playlistService.deletePlaylist(playlistId);
    }
}
