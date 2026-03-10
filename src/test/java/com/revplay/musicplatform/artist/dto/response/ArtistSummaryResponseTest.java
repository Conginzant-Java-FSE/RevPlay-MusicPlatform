package com.revplay.musicplatform.artist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistSummaryResponseTest {

    @Test
    @DisplayName("setters and getters expose summary fields")
    void settersAndGettersExposeFields() {
        ArtistSummaryResponse response = new ArtistSummaryResponse();
        response.setArtistId(1L);
        response.setSongCount(10L);
        response.setAlbumCount(5L);
        response.setPodcastCount(2L);

        assertThat(response.getArtistId()).isEqualTo(1L);
        assertThat(response.getSongCount()).isEqualTo(10L);
        assertThat(response.getAlbumCount()).isEqualTo(5L);
        assertThat(response.getPodcastCount()).isEqualTo(2L);
    }
}
