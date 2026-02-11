package com.revplay.revplay.dto;

import com.revplay.revplay.enums.SocialPlatform;

public record ArtistSocialLinkResponseDTO(
        Long linkId,
        SocialPlatform platform,
        String url
) {}
