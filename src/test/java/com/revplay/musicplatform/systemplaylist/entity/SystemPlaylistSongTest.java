package com.revplay.musicplatform.systemplaylist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SystemPlaylistSongTest {

    @Test
    @DisplayName("setters and getters store all fields")
    void settersAndGettersStoreFields() {
        SystemPlaylist playlist = new SystemPlaylist();
        playlist.setId(100L);
        LocalDateTime deletedAt = LocalDateTime.of(2026, 3, 9, 18, 0);

        SystemPlaylistSong song = new SystemPlaylistSong();
        song.setId(1L);
        song.setSystemPlaylist(playlist);
        song.setSongId(200L);
        song.setPosition(1);
        song.setDeletedAt(deletedAt);

        assertThat(song.getId()).isEqualTo(1L);
        assertThat(song.getSystemPlaylist()).isSameAs(playlist);
        assertThat(song.getSongId()).isEqualTo(200L);
        assertThat(song.getPosition()).isEqualTo(1);
        assertThat(song.getDeletedAt()).isEqualTo(deletedAt);
    }
}
