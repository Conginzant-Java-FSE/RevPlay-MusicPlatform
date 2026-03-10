package com.revplay.musicplatform.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlaylistSongTest {

    @Test
    @DisplayName("builder and accessors expose playlist song fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime addedAt = LocalDateTime.of(2026, 3, 9, 7, 0);
        PlaylistSong playlistSong = PlaylistSong.builder()
                .id(1L)
                .playlistId(2L)
                .songId(3L)
                .position(4)
                .addedAt(addedAt)
                .version(5L)
                .build();

        assertThat(playlistSong.getId()).isEqualTo(1L);
        assertThat(playlistSong.getPlaylistId()).isEqualTo(2L);
        assertThat(playlistSong.getSongId()).isEqualTo(3L);
        assertThat(playlistSong.getPosition()).isEqualTo(4);
        assertThat(playlistSong.getAddedAt()).isEqualTo(addedAt);
        assertThat(playlistSong.getVersion()).isEqualTo(5L);
    }
}
