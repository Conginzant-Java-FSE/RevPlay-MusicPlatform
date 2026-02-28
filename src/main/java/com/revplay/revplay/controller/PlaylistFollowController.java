package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.PlaylistFollowCreateRequest;
import com.revplay.revplay.dto.response.PlaylistFollowResponse;
import com.revplay.revplay.service.PlaylistFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlist-follows")
@RequiredArgsConstructor
public class PlaylistFollowController {

    private final PlaylistFollowService playlistFollowService;

    @PostMapping
    public PlaylistFollowResponse follow(@RequestBody PlaylistFollowCreateRequest request) {
        return playlistFollowService.followPlaylist(request);
    }

    @DeleteMapping("/{userId}/{playlistId}")
    public void unfollow(@PathVariable Long userId,
                         @PathVariable Long playlistId) {
        playlistFollowService.unfollowPlaylist(userId, playlistId);
    }

    @GetMapping("/user/{userId}")
    public List<PlaylistFollowResponse> getUserFollows(@PathVariable Long userId) {
        return playlistFollowService.getUserFollows(userId);
    }
}