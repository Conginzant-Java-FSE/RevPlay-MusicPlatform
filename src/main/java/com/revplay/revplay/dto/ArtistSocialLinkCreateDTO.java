package com.revplay.revplay.dto;

import com.revplay.revplay.enums.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ArtistSocialLinkCreateDTO(
        @NotNull SocialPlatform platform,

        @NotBlank
        @Size(max = 500)
        String url
) {}

