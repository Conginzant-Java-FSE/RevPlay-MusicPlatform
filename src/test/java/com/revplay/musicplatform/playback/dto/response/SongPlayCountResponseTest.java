package com.revplay.musicplatform.playback.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SongPlayCountResponseTest {

    @Test
    @DisplayName("record exposes song play count response components")
    void recordExposesComponents() {
        SongPlayCountResponse response = new SongPlayCountResponse(88L, "Track", 999L);

        assertThat(response.songId()).isEqualTo(88L);
        assertThat(response.title()).isEqualTo("Track");
        assertThat(response.playCount()).isEqualTo(999L);
    }
}
