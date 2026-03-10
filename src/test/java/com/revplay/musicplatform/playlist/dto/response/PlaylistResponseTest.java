package com.revplay.musicplatform.playlist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlaylistResponseTest {

    @Test
    @DisplayName("builder and accessors expose playlist response fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 9, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 9, 10, 5);
        PlaylistResponse response = PlaylistResponse.builder()
                .id(1L)
                .userId(2L)
                .name("Mix")
                .description("Desc")
                .isPublic(Boolean.TRUE)
                .songCount(3L)
                .followerCount(4L)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Mix");
        assertThat(response.getDescription()).isEqualTo("Desc");
        assertThat(response.getIsPublic()).isTrue();
        assertThat(response.getSongCount()).isEqualTo(3L);
        assertThat(response.getFollowerCount()).isEqualTo(4L);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
