package com.revplay.revplay.service.Impl;

import com.revplay.revplay.dto.request.PlaylistFollowCreateRequest;
import com.revplay.revplay.dto.response.PlaylistFollowResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistFollow;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.mapper.PlaylistFollowMapper;
import com.revplay.revplay.repository.PlaylistFollowRepository;
import com.revplay.revplay.repository.PlaylistRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.service.PlaylistFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistFollowServiceImpl implements PlaylistFollowService {

    private final PlaylistFollowRepository playlistFollowRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @Override
    public PlaylistFollowResponse followPlaylist(PlaylistFollowCreateRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = playlistRepository.findById(request.getPlaylistId())
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        PlaylistFollow follow = PlaylistFollowMapper.toEntity(user, playlist);

        return PlaylistFollowMapper.toResponse(
                playlistFollowRepository.save(follow)
        );
    }

    @Override
    public void unfollowPlaylist(Long userId, Long playlistId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        playlistFollowRepository.deleteByUserAndPlaylist(user, playlist);
    }

    @Override
    public List<PlaylistFollowResponse> getUserFollows(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return playlistFollowRepository.findByUser(user)
                .stream()
                .map(PlaylistFollowMapper::toResponse)
                .toList();
    }
}