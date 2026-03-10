package com.revplay.musicplatform.catalog.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ContentVisibilityTest {

    @Test
    @DisplayName("ContentVisibility contains expected constants")
    void containsExpectedConstants() {
        EnumSet<ContentVisibility> values = EnumSet.allOf(ContentVisibility.class);

        assertThat(values).containsExactlyInAnyOrder(ContentVisibility.PUBLIC, ContentVisibility.UNLISTED);
    }
}
