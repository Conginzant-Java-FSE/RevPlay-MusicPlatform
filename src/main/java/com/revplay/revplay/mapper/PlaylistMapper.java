package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.request.PlaylistCreateRequest;
import com.revplay.revplay.dto.request.PlaylistUpdateRequest;
import com.revplay.revplay.dto.response.PlaylistResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.User;

public class PlaylistMapper {

    private PlaylistMapper() {

    }


    public static Playlist toEntity(PlaylistCreateRequest request, User user) {

        Playlist playlist = new Playlist();
        playlist.setTitle(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setIsPublic(
                request.getIsPublic() != null ? request.getIsPublic() : false
        );
        playlist.setUser(user);

        return playlist;
    }


    public static void updateEntity(Playlist playlist, PlaylistUpdateRequest request) {

        if (request.getName() != null) {
            playlist.setTitle(request.getName());
        }

        if (request.getDescription() != null) {
            playlist.setDescription(request.getDescription());
        }

        if (request.getIsPublic() != null) {
            playlist.setIsPublic(request.getIsPublic());
        }
    }


    public static PlaylistResponse toResponse(Playlist playlist) {

        return PlaylistResponse.builder()
                .id(playlist.getPlaylistId())
                .name(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.getIsPublic())
                .createdBy(playlist.getUser().getUserId())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(null) // You don’t have updatedAt field in entity
                .build();
    }
}
