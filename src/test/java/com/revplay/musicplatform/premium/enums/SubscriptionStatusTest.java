package com.revplay.musicplatform.premium.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SubscriptionStatusTest {

    @Test
    @DisplayName("enum contains expected subscription statuses")
    void enumContainsExpectedValues() {
        assertThat(SubscriptionStatus.values())
                .containsExactly(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED, SubscriptionStatus.CANCELLED);
    }
}
