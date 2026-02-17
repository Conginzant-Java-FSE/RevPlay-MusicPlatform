package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.ArtistSocialLinkResponse;
import com.revplay.revplay.entity.ArtistSocialLink;
import org.springframework.stereotype.Component;

@Component
public class ArtistSocialLinkMapper {

    public ArtistSocialLinkResponse toResponse(ArtistSocialLink link) {
        if (link == null) return null;

        ArtistSocialLinkResponse dto = new ArtistSocialLinkResponse();
        dto.setLinkId(link.getLinkId());
        dto.setArtistId(link.getArtist().getArtistId());
        dto.setPlatform(link.getPlatform());
        dto.setUrl(link.getUrl());
        return dto;
    }
}

