
package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.request.AlbumCreateRequest;
import com.revplay.revplay.dto.response.AlbumResponse;
import com.revplay.revplay.dto.request.AlbumUpdateRequest;
import com.revplay.revplay.entity.Album;
import com.revplay.revplay.entity.Artist;
import org.springframework.stereotype.Component;

@Component
public class AlbumMapper {

    public Album toEntity(AlbumCreateRequest request, Artist artist) {
        Album album = new Album();
        album.setArtist(artist);
        album.setTitle(request.getTitle());
        album.setDescription(request.getDescription());
        album.setCoverArtUrl(request.getCoverArtUrl());
        album.setReleaseDate(request.getReleaseDate());
        return album;
    }

    // safe partial update (same logic moved)
    public void applyUpdate(AlbumUpdateRequest request, Album album) {
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            album.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            album.setDescription(request.getDescription());
        }
        if (request.getCoverArtUrl() != null) {
            album.setCoverArtUrl(request.getCoverArtUrl());
        }
        if (request.getReleaseDate() != null) {
            album.setReleaseDate(request.getReleaseDate());
        }
    }

    public AlbumResponse toResponse(Album album) {
        AlbumResponse dto = new AlbumResponse();
        dto.setAlbumId(album.getAlbumId());
        dto.setArtistId(album.getArtist().getArtistId());
        dto.setTitle(album.getTitle());
        dto.setDescription(album.getDescription());
        dto.setCoverArtUrl(album.getCoverArtUrl());
        dto.setReleaseDate(album.getReleaseDate());
        return dto;
    }
}
