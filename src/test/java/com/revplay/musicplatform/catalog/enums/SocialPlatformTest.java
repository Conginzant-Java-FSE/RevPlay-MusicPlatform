package com.revplay.musicplatform.catalog.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SocialPlatformTest {

    @Test
    @DisplayName("SocialPlatform contains expected constants")
    void containsExpectedConstants() {
        EnumSet<SocialPlatform> values = EnumSet.allOf(SocialPlatform.class);

        assertThat(values).contains(
                SocialPlatform.INSTAGRAM,
                SocialPlatform.TWITTER,
                SocialPlatform.YOUTUBE,
                SocialPlatform.SPOTIFY,
                SocialPlatform.WEBSITE,
                SocialPlatform.OTHER
        );
    }
}
