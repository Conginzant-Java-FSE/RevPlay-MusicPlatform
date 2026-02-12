package com.revplay.revplay.dto.response;

import com.revplay.revplay.enums.SocialPlatform;

public record ArtistSocialLinkResponseDTO(
        Long linkId,
        SocialPlatform platform,
        String url
) {}
