package com.revplay.musicplatform.playlist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlaylistSongResponseTest {

    @Test
    @DisplayName("builder and accessors expose playlist song response fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime addedAt = LocalDateTime.of(2026, 3, 9, 12, 0);
        PlaylistSongResponse response = PlaylistSongResponse.builder()
                .id(1L)
                .playlistId(2L)
                .songId(3L)
                .position(4)
                .addedAt(addedAt)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPlaylistId()).isEqualTo(2L);
        assertThat(response.getSongId()).isEqualTo(3L);
        assertThat(response.getPosition()).isEqualTo(4);
        assertThat(response.getAddedAt()).isEqualTo(addedAt);
    }
}
