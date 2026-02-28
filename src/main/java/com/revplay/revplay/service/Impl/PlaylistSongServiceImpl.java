package com.revplay.revplay.service.Impl;

import com.revplay.revplay.dto.request.PlaylistSongCreateRequest;
import com.revplay.revplay.dto.response.PlaylistSongResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistSong;
import com.revplay.revplay.entity.Song;
import com.revplay.revplay.mapper.PlaylistSongMapper;
import com.revplay.revplay.repository.PlaylistRepository;
import com.revplay.revplay.repository.PlaylistSongRepository;
import com.revplay.revplay.repository.SongRepository;
import com.revplay.revplay.service.PlaylistSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistSongServiceImpl implements PlaylistSongService {

    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    @Override
    public PlaylistSongResponse addSongToPlaylist(PlaylistSongCreateRequest request) {

        Playlist playlist = playlistRepository.findById(request.getPlaylistId())
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new RuntimeException("Song not found"));

        // Check if already exists
        PlaylistSong existing = playlistSongRepository
                .findByPlaylistAndSong(playlist, song);

        if (existing != null) {
            throw new RuntimeException("Song already exists in playlist");
        }

        // Auto-calculate position
        int position = playlistSongRepository
                .findByPlaylistOrderByPositionAsc(playlist)
                .size() + 1;

        PlaylistSong playlistSong =
                PlaylistSongMapper.toEntity(playlist, song, position);

        PlaylistSong saved = playlistSongRepository.save(playlistSong);

        return PlaylistSongMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistSongResponse> getSongsByPlaylist(Long playlistId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        return playlistSongRepository
                .findByPlaylistOrderByPositionAsc(playlist)
                .stream()
                .map(PlaylistSongMapper::toResponse)
                .toList();
    }

    @Override
    public void removeSongFromPlaylist(Long playlistId, Long songId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        PlaylistSong playlistSong =
                playlistSongRepository.findByPlaylistAndSong(playlist, song);

        if (playlistSong == null) {
            throw new RuntimeException("Song not found in playlist");
        }

        playlistSongRepository.delete(playlistSong);
    }

    @Override
    public void removeAllSongsFromPlaylist(Long playlistId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        playlistSongRepository.deleteByPlaylist(playlist);
    }
}