package com.revplay.musicplatform.playlist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserLikeResponseTest {

    @Test
    @DisplayName("builder and accessors expose user like response fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 9, 14, 0);
        UserLikeResponse response = UserLikeResponse.builder()
                .id(1L)
                .userId(2L)
                .likeableId(3L)
                .likeableType("PODCAST")
                .createdAt(createdAt)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getLikeableId()).isEqualTo(3L);
        assertThat(response.getLikeableType()).isEqualTo("PODCAST");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }
}
