package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.PlaylistSongResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistSong;
import com.revplay.revplay.entity.Song;

import java.time.LocalDateTime;

public class PlaylistSongMapper {

    private PlaylistSongMapper() {

    }


    public static PlaylistSongResponse toResponse(PlaylistSong playlistSong) {

        return PlaylistSongResponse.builder()
                .id(playlistSong.getPlaylistSongId())
                .playlistId(playlistSong.getPlaylist().getPlaylistId())
                .songId(playlistSong.getSong().getSongId())
                .addedAt(LocalDateTime.now())
                .build();
    }


    public static PlaylistSong toEntity(Playlist playlist, Song song, Integer position) {

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        playlistSong.setPosition(position);

        return playlistSong;
    }
}
