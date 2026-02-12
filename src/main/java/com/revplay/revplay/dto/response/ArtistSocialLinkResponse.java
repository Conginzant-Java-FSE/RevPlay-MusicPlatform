package com.revplay.revplay.dto.response;

import com.revplay.revplay.enums.SocialPlatform;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistSocialLinkResponse {

    private Long linkId;
    private Long artistId;
    private SocialPlatform platform;
    private String url;
}

