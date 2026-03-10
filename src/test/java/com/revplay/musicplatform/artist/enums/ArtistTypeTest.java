package com.revplay.musicplatform.artist.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ArtistTypeTest {

    @Test
    @DisplayName("ArtistType contains expected enum constants")
    void containsExpectedEnumConstants() {
        EnumSet<ArtistType> values = EnumSet.allOf(ArtistType.class);

        assertThat(values).containsExactlyInAnyOrder(ArtistType.MUSIC, ArtistType.PODCAST, ArtistType.BOTH);
    }
}
