package com.revplay.musicplatform.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlaylistTest {

    @Test
    @DisplayName("builder applies default flags when not explicitly set")
    void builderAppliesDefaultFlags() {
        Playlist playlist = Playlist.builder()
                .id(1L)
                .userId(11L)
                .name("Mix")
                .description("desc")
                .build();

        assertThat(playlist.getIsPublic()).isTrue();
        assertThat(playlist.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("setters and getters store all playlist fields")
    void settersAndGettersStoreFields() {
        Playlist playlist = new Playlist();
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 9, 6, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 9, 6, 10);

        playlist.setId(2L);
        playlist.setUserId(22L);
        playlist.setName("Road Trip");
        playlist.setDescription("Hits");
        playlist.setIsPublic(Boolean.FALSE);
        playlist.setIsActive(Boolean.FALSE);
        playlist.setCreatedAt(createdAt);
        playlist.setUpdatedAt(updatedAt);
        playlist.setVersion(3L);

        assertThat(playlist.getId()).isEqualTo(2L);
        assertThat(playlist.getUserId()).isEqualTo(22L);
        assertThat(playlist.getName()).isEqualTo("Road Trip");
        assertThat(playlist.getDescription()).isEqualTo("Hits");
        assertThat(playlist.getIsPublic()).isFalse();
        assertThat(playlist.getIsActive()).isFalse();
        assertThat(playlist.getCreatedAt()).isEqualTo(createdAt);
        assertThat(playlist.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(playlist.getVersion()).isEqualTo(3L);
    }
}
