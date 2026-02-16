package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.request.ArtistCreateRequest;
import com.revplay.revplay.dto.response.ArtistResponse;
import com.revplay.revplay.dto.request.ArtistUpdateRequest;
import com.revplay.revplay.entity.Artist;
import org.springframework.stereotype.Component;

@Component
public class ArtistMapper {

    public ArtistResponse toResponse(Artist artist) {
        if (artist == null) return null;

        ArtistResponse dto = new ArtistResponse();
        dto.setArtistId(artist.getArtistId());
        dto.setUserId(artist.getUserId());
        dto.setDisplayName(artist.getDisplayName());
        dto.setBio(artist.getBio());
        dto.setArtistType(artist.getArtistType());
        dto.setVerified(artist.getVerified());
        dto.setCreatedAt(artist.getCreatedAt());
        dto.setUpdatedAt(artist.getUpdatedAt());
        return dto;
    }

    public Artist toEntity(ArtistCreateRequest request, Long userId) {
        if (request == null) return null;

        Artist artist = new Artist();
        artist.setUserId(userId);
        artist.setDisplayName(request.getDisplayName());
        artist.setBio(request.getBio());
        artist.setArtistType(request.getArtistType());
        artist.setVerified(Boolean.FALSE);
        return artist;
    }

    public void updateEntity(ArtistUpdateRequest request, Artist artist) {
        if (request == null || artist == null) return;

        artist.setDisplayName(request.getDisplayName());
        artist.setBio(request.getBio());
        artist.setArtistType(request.getArtistType());
        // verified, userId, createdAt untouched
        // updatedAt automatically handled by @PreUpdate in entity
    }
}
