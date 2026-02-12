package com.revplay.revplay.dto.request;

import com.revplay.revplay.enums.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistSocialLinkCreate {

        @NotNull(message = "platform is required")
        private SocialPlatform platform;

        @NotBlank(message = "url is required")
        @Size(max = 500, message = "url must be at most 500 characters")
        private String url;
}
