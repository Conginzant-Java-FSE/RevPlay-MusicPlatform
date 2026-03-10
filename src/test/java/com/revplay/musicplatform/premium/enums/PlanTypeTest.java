package com.revplay.musicplatform.premium.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PlanTypeTest {

    @Test
    @DisplayName("enum contains expected plan types")
    void enumContainsExpectedValues() {
        assertThat(PlanType.values()).containsExactly(PlanType.MONTHLY, PlanType.YEARLY);
    }
}
