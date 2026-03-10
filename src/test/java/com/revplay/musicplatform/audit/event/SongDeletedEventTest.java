package com.revplay.musicplatform.audit.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SongDeletedEventTest {

    @Test
    @DisplayName("SongDeletedEvent stores song id")
    void storesSongId() {
        SongDeletedEvent event = new SongDeletedEvent(14L);

        assertThat(event.getSongId()).isEqualTo(14L);
    }
}
