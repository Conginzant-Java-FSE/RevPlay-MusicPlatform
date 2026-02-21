package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.PlaylistCreateRequest;
import com.revplay.revplay.dto.request.PlaylistUpdateRequest;
import com.revplay.revplay.dto.response.PlaylistResponse;

import java.util.List;

public interface PlaylistService {

    PlaylistResponse createPlaylist(Long userId, PlaylistCreateRequest request);

    PlaylistResponse updatePlaylist(Long playlistId, PlaylistUpdateRequest request);

    PlaylistResponse getPlaylistById(Long playlistId);

    List<PlaylistResponse> getPlaylistsByUser(Long userId);

    List<PlaylistResponse> getPublicPlaylists();

    void deletePlaylist(Long playlistId);
}
