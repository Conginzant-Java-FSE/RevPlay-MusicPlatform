package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.PlaylistFollowResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistFollow;
import com.revplay.revplay.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PlaylistFollowMapper {


    public PlaylistFollow toEntity(User user, Playlist playlist) {

        PlaylistFollow playlistFollow = new PlaylistFollow();
        playlistFollow.setUser(user);
        playlistFollow.setPlaylist(playlist);

        return playlistFollow;
    }


    public PlaylistFollowResponse toResponse(PlaylistFollow playlistFollow) {

        return PlaylistFollowResponse.builder()
                .id(playlistFollow.getFollowId())
                .playlistId(playlistFollow.getPlaylist().getPlaylistId())
                .userId(playlistFollow.getUser().getUserId())
                .followedAt(null) // Not present in entity
                .build();
    }
}
