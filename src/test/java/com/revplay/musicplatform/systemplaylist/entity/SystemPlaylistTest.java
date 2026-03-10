package com.revplay.musicplatform.systemplaylist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SystemPlaylistTest {

    @Test
    @DisplayName("default isActive is true")
    void defaultIsActiveIsTrue() {
        SystemPlaylist playlist = new SystemPlaylist();

        assertThat(playlist.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("setters and getters store all fields")
    void settersAndGettersStoreFields() {
        SystemPlaylist playlist = new SystemPlaylist();
        LocalDateTime deletedAt = LocalDateTime.of(2026, 3, 9, 17, 0);

        playlist.setId(1L);
        playlist.setName("Telugu Mix");
        playlist.setSlug("telugu-mix");
        playlist.setDescription("Top Telugu tracks");
        playlist.setIsActive(Boolean.FALSE);
        playlist.setDeletedAt(deletedAt);

        assertThat(playlist.getId()).isEqualTo(1L);
        assertThat(playlist.getName()).isEqualTo("Telugu Mix");
        assertThat(playlist.getSlug()).isEqualTo("telugu-mix");
        assertThat(playlist.getDescription()).isEqualTo("Top Telugu tracks");
        assertThat(playlist.getIsActive()).isFalse();
        assertThat(playlist.getDeletedAt()).isEqualTo(deletedAt);
    }
}
