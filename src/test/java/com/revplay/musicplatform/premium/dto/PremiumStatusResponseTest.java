package com.revplay.musicplatform.premium.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PremiumStatusResponseTest {

    @Test
    @DisplayName("record exposes premium status components")
    void recordExposesComponents() {
        LocalDateTime expiryDate = LocalDateTime.of(2026, 3, 9, 15, 0);
        PremiumStatusResponse response = new PremiumStatusResponse(true, expiryDate);

        assertThat(response.isPremium()).isTrue();
        assertThat(response.expiryDate()).isEqualTo(expiryDate);
    }
}
