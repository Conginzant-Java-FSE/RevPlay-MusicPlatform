package com.revplay.revplay.service.Impl;

import com.revplay.revplay.dto.request.PlaylistCreateRequest;
import com.revplay.revplay.dto.request.PlaylistUpdateRequest;
import com.revplay.revplay.dto.response.PlaylistResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.repository.PlaylistRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.service.PlaylistService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository,
                               UserRepository userRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PlaylistResponse createPlaylist(Long userId, PlaylistCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setTitle(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);

        Playlist saved = playlistRepository.save(playlist);

        return mapToResponse(saved);
    }

    @Override
    public PlaylistResponse updatePlaylist(Long playlistId, PlaylistUpdateRequest request) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if (request.getName() != null) {
            playlist.setTitle(request.getName());
        }

        if (request.getDescription() != null) {
            playlist.setDescription(request.getDescription());
        }

        if (request.getIsPublic() != null) {
            playlist.setIsPublic(request.getIsPublic());
        }

        Playlist updated = playlistRepository.save(playlist);

        return mapToResponse(updated);
    }

    @Override
    public PlaylistResponse getPlaylistById(Long playlistId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        return mapToResponse(playlist);
    }

    @Override
    public List<PlaylistResponse> getPlaylistsByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return playlistRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlaylistResponse> getPublicPlaylists() {

        return playlistRepository.findByIsPublicTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePlaylist(Long playlistId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        playlistRepository.delete(playlist);
    }

    private PlaylistResponse mapToResponse(Playlist playlist) {

        return PlaylistResponse.builder()
                .id(playlist.getPlaylistId())
                .name(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.getIsPublic())
                .createdBy(playlist.getUser().getUserId())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(null)
                .build();
    }
}
