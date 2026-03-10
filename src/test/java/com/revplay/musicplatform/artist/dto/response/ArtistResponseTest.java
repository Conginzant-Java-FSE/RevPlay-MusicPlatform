package com.revplay.musicplatform.artist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.artist.enums.ArtistType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistResponseTest {

    @Test
    @DisplayName("setters and getters expose all response fields")
    void settersAndGettersExposeFields() {
        ArtistResponse response = new ArtistResponse();
        LocalDateTime now = LocalDateTime.now();
        response.setArtistId(1L);
        response.setUserId(2L);
        response.setDisplayName("Artist");
        response.setBio("Bio");
        response.setBannerImageUrl("/img");
        response.setArtistType(ArtistType.MUSIC);
        response.setVerified(Boolean.TRUE);
        response.setCreatedAt(now);
        response.setUpdatedAt(now);

        assertThat(response.getArtistId()).isEqualTo(1L);
        assertThat(response.getArtistType()).isEqualTo(ArtistType.MUSIC);
        assertThat(response.getVerified()).isTrue();
    }
}
