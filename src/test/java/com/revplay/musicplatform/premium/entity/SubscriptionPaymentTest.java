package com.revplay.musicplatform.premium.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.premium.enums.PaymentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class SubscriptionPaymentTest {

    @Test
    @DisplayName("setters and getters store all payment fields")
    void settersAndGettersStoreFields() {
        SubscriptionPayment payment = new SubscriptionPayment();
        LocalDateTime paidAt = LocalDateTime.of(2026, 3, 9, 16, 0);

        payment.setId(1L);
        payment.setUserId(2L);
        payment.setSubscriptionId(3L);
        payment.setAmount(199.0);
        payment.setCurrency("INR");
        payment.setPaymentMethod("DUMMY");
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference("TX-123");
        payment.setPaidAt(paidAt);

        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getUserId()).isEqualTo(2L);
        assertThat(payment.getSubscriptionId()).isEqualTo(3L);
        assertThat(payment.getAmount()).isEqualTo(199.0);
        assertThat(payment.getCurrency()).isEqualTo("INR");
        assertThat(payment.getPaymentMethod()).isEqualTo("DUMMY");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getTransactionReference()).isEqualTo("TX-123");
        assertThat(payment.getPaidAt()).isEqualTo(paidAt);
    }
}
