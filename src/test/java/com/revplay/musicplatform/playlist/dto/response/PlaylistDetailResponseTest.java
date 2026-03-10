package com.revplay.musicplatform.playlist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlaylistDetailResponseTest {

    @Test
    @DisplayName("builder and accessors expose playlist detail fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 9, 11, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 9, 11, 5);
        PlaylistSongResponse song = PlaylistSongResponse.builder().id(10L).playlistId(1L).songId(20L).position(1).build();

        PlaylistDetailResponse response = PlaylistDetailResponse.builder()
                .id(1L)
                .userId(2L)
                .name("Detail")
                .description("Desc")
                .isPublic(Boolean.FALSE)
                .songCount(1L)
                .followerCount(2L)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .songs(List.of(song))
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Detail");
        assertThat(response.getIsPublic()).isFalse();
        assertThat(response.getSongs()).hasSize(1);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
