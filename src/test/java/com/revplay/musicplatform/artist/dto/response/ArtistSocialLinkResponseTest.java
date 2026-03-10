package com.revplay.musicplatform.artist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.catalog.enums.SocialPlatform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistSocialLinkResponseTest {

    @Test
    @DisplayName("setters and getters expose social link fields")
    void settersAndGettersExposeFields() {
        ArtistSocialLinkResponse response = new ArtistSocialLinkResponse();
        response.setLinkId(1L);
        response.setArtistId(2L);
        response.setPlatform(SocialPlatform.OTHER);
        response.setUrl("https://other.com/a");

        assertThat(response.getLinkId()).isEqualTo(1L);
        assertThat(response.getPlatform()).isEqualTo(SocialPlatform.OTHER);
    }
}
