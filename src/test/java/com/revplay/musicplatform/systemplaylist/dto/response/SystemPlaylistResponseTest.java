package com.revplay.musicplatform.systemplaylist.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SystemPlaylistResponseTest {

    @Test
    @DisplayName("builder and accessors expose response fields")
    void builderAndAccessorsExposeFields() {
        SystemPlaylistResponse response = SystemPlaylistResponse.builder()
                .id(1L)
                .name("DJ Mix")
                .slug("dj-mix")
                .description("High energy tracks")
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("DJ Mix");
        assertThat(response.getSlug()).isEqualTo("dj-mix");
        assertThat(response.getDescription()).isEqualTo("High energy tracks");

        response.setDescription("Updated");
        assertThat(response.getDescription()).isEqualTo("Updated");
    }
}
