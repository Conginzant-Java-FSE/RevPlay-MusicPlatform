package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.PlaylistFollowResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistFollow;
import com.revplay.revplay.entity.User;

import java.time.LocalDateTime;

public class PlaylistFollowMapper {

    private PlaylistFollowMapper() {

    }

    public static PlaylistFollowResponse toResponse(PlaylistFollow follow) {

        return PlaylistFollowResponse.builder()
                .id(follow.getFollowId())
                .playlistId(follow.getPlaylist().getPlaylistId())
                .userId(follow.getUser().getUserId())
                .followedAt(LocalDateTime.now())
                .build();
    }


    public static PlaylistFollow toEntity(User user, Playlist playlist) {

        PlaylistFollow follow = new PlaylistFollow();
        follow.setUser(user);
        follow.setPlaylist(playlist);

        return follow;
    }
}
