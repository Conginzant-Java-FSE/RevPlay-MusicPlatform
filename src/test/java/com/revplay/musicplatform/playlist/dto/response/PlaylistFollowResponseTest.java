package com.revplay.musicplatform.playlist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlaylistFollowResponseTest {

    @Test
    @DisplayName("builder and accessors expose playlist follow response fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime followedAt = LocalDateTime.of(2026, 3, 9, 13, 0);
        PlaylistFollowResponse response = PlaylistFollowResponse.builder()
                .id(1L)
                .playlistId(2L)
                .followerUserId(3L)
                .followedAt(followedAt)
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPlaylistId()).isEqualTo(2L);
        assertThat(response.getFollowerUserId()).isEqualTo(3L);
        assertThat(response.getFollowedAt()).isEqualTo(followedAt);
    }
}
