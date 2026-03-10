package com.revplay.musicplatform.artist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.catalog.enums.SocialPlatform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistSocialLinkTest {

    @Test
    @DisplayName("ArtistSocialLink stores and returns fields")
    void storesAndReturnsFields() {
        ArtistSocialLink link = new ArtistSocialLink();
        link.setLinkId(1L);
        link.setArtistId(2L);
        link.setPlatform(SocialPlatform.INSTAGRAM);
        link.setUrl("https://instagram.com/a");
        link.setVersion(0L);

        assertThat(link.getLinkId()).isEqualTo(1L);
        assertThat(link.getArtistId()).isEqualTo(2L);
        assertThat(link.getPlatform()).isEqualTo(SocialPlatform.INSTAGRAM);
        assertThat(link.getUrl()).isEqualTo("https://instagram.com/a");
        assertThat(link.getVersion()).isZero();
    }
}
