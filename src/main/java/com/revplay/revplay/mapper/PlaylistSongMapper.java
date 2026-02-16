package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.request.PlaylistSongCreateRequest;
import com.revplay.revplay.dto.response.PlaylistSongResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistSong;
import com.revplay.revplay.entity.Song;
import org.springframework.stereotype.Component;

@Component
public class PlaylistSongMapper {


    public PlaylistSong toEntity(Playlist playlist,
                                 Song song,
                                 Integer position) {

        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        playlistSong.setPosition(position);

        return playlistSong;
    }


    public PlaylistSongResponse toResponse(PlaylistSong playlistSong) {

        return PlaylistSongResponse.builder()
                .id(playlistSong.getPlaylistSongId())
                .playlistId(playlistSong.getPlaylist().getPlaylistId())
                .songId(playlistSong.getSong().getSongId())
                .addedAt(null) // not in entity
                .build();
    }
}
