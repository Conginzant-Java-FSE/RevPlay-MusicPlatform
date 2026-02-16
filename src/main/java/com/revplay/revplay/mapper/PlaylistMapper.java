package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.request.PlaylistCreateRequest;
import com.revplay.revplay.dto.request.PlaylistUpdateRequest;
import com.revplay.revplay.dto.response.PlaylistResponse;
import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PlaylistMapper {


    public Playlist toEntity(PlaylistCreateRequest request, User user) {

        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setTitle(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setIsPublic(
                request.getIsPublic() != null ? request.getIsPublic() : false
        );

        return playlist;
    }


    public void updateEntity(PlaylistUpdateRequest request, Playlist playlist) {

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


    public PlaylistResponse toResponse(Playlist playlist) {

        return PlaylistResponse.builder()
                .id(playlist.getPlaylistId())
                .name(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.getIsPublic())
                .createdBy(playlist.getUser().getUserId())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(null) // not in entity
                .build();
    }
}

