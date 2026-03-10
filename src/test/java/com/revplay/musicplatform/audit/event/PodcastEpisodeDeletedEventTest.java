package com.revplay.musicplatform.audit.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PodcastEpisodeDeletedEventTest {

    @Test
    @DisplayName("PodcastEpisodeDeletedEvent stores episode id")
    void storesEpisodeId() {
        PodcastEpisodeDeletedEvent event = new PodcastEpisodeDeletedEvent(13L);

        assertThat(event.getEpisodeId()).isEqualTo(13L);
    }
}
