package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.PlaylistSongCreateRequest;
import com.revplay.revplay.dto.response.PlaylistSongResponse;

import java.util.List;

public interface PlaylistSongService {

    PlaylistSongResponse addSongToPlaylist(PlaylistSongCreateRequest request);

    List<PlaylistSongResponse> getSongsByPlaylist(Long playlistId);

    void removeSongFromPlaylist(Long playlistId, Long songId);

    void removeAllSongsFromPlaylist(Long playlistId);
}