package com.revplay.musicplatform.audit.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AlbumDeletedEventTest {

    @Test
    @DisplayName("AlbumDeletedEvent stores album id")
    void storesAlbumId() {
        AlbumDeletedEvent event = new AlbumDeletedEvent(11L);

        assertThat(event.getAlbumId()).isEqualTo(11L);
    }
}
