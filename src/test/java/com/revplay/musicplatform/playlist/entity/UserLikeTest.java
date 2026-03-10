package com.revplay.musicplatform.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserLikeTest {

    @Test
    @DisplayName("builder and accessors expose user like fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 9, 9, 0);
        UserLike userLike = UserLike.builder()
                .id(10L)
                .userId(11L)
                .likeableId(12L)
                .likeableType("SONG")
                .createdAt(createdAt)
                .version(13L)
                .build();

        assertThat(userLike.getId()).isEqualTo(10L);
        assertThat(userLike.getUserId()).isEqualTo(11L);
        assertThat(userLike.getLikeableId()).isEqualTo(12L);
        assertThat(userLike.getLikeableType()).isEqualTo("SONG");
        assertThat(userLike.getCreatedAt()).isEqualTo(createdAt);
        assertThat(userLike.getVersion()).isEqualTo(13L);
    }
}
