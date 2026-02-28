package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.PlaylistFollowCreateRequest;
import com.revplay.revplay.dto.response.PlaylistFollowResponse;

import java.util.List;

public interface PlaylistFollowService {

    PlaylistFollowResponse followPlaylist(PlaylistFollowCreateRequest request);

    void unfollowPlaylist(Long userId, Long playlistId);

    List<PlaylistFollowResponse> getUserFollows(Long userId);
}