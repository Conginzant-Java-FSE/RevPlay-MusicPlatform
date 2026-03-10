package com.revplay.musicplatform.premium.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PaymentStatusTest {

    @Test
    @DisplayName("enum contains expected payment statuses")
    void enumContainsExpectedValues() {
        assertThat(PaymentStatus.values()).containsExactly(PaymentStatus.SUCCESS, PaymentStatus.FAILED);
    }
}
