package com.revplay.musicplatform.playlist.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlaylistFollowTest {

    @Test
    @DisplayName("builder and accessors expose playlist follow fields")
    void builderAndAccessorsExposeFields() {
        LocalDateTime followedAt = LocalDateTime.of(2026, 3, 9, 8, 0);
        PlaylistFollow follow = PlaylistFollow.builder()
                .id(6L)
                .playlistId(7L)
                .followerUserId(8L)
                .followedAt(followedAt)
                .version(9L)
                .build();

        assertThat(follow.getId()).isEqualTo(6L);
        assertThat(follow.getPlaylistId()).isEqualTo(7L);
        assertThat(follow.getFollowerUserId()).isEqualTo(8L);
        assertThat(follow.getFollowedAt()).isEqualTo(followedAt);
        assertThat(follow.getVersion()).isEqualTo(9L);
    }
}
