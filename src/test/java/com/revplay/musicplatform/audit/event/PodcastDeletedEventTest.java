package com.revplay.musicplatform.audit.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PodcastDeletedEventTest {

    @Test
    @DisplayName("PodcastDeletedEvent stores podcast id")
    void storesPodcastId() {
        PodcastDeletedEvent event = new PodcastDeletedEvent(12L);

        assertThat(event.getPodcastId()).isEqualTo(12L);
    }
}
