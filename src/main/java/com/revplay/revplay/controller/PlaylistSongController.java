package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.PlaylistSongCreateRequest;
import com.revplay.revplay.dto.response.PlaylistSongResponse;
import com.revplay.revplay.service.PlaylistSongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlist-songs")
@RequiredArgsConstructor
public class PlaylistSongController {

    private final PlaylistSongService playlistSongService;

    // Add song
    @PostMapping
    public ResponseEntity<PlaylistSongResponse> addSong(
            @Valid @RequestBody PlaylistSongCreateRequest request) {

        PlaylistSongResponse response =
                playlistSongService.addSongToPlaylist(request);

        return ResponseEntity.ok(response);
    }

    // Get songs by playlist
    @GetMapping("/{playlistId}")
    public ResponseEntity<List<PlaylistSongResponse>> getSongs(
            @PathVariable Long playlistId) {

        return ResponseEntity.ok(
                playlistSongService.getSongsByPlaylist(playlistId)
        );
    }

    // Remove specific song
    @DeleteMapping("/{playlistId}/{songId}")
    public ResponseEntity<Void> removeSong(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {

        playlistSongService.removeSongFromPlaylist(playlistId, songId);
        return ResponseEntity.noContent().build();
    }

    // Remove all songs from playlist
    @DeleteMapping("/playlist/{playlistId}")
    public ResponseEntity<Void> removeAllSongs(
            @PathVariable Long playlistId) {

        playlistSongService.removeAllSongsFromPlaylist(playlistId);
        return ResponseEntity.noContent().build();
    }
}