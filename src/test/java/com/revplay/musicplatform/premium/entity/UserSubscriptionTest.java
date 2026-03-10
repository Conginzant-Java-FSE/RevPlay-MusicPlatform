package com.revplay.musicplatform.premium.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.revplay.musicplatform.premium.enums.PlanType;
import com.revplay.musicplatform.premium.enums.SubscriptionStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserSubscriptionTest {

    @Test
    @DisplayName("prePersist sets createdAt and updatedAt")
    void prePersistSetsTimestamps() {
        UserSubscription subscription = new UserSubscription();

        subscription.prePersist();

        assertThat(subscription.getCreatedAt()).isNotNull();
        assertThat(subscription.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("preUpdate refreshes updatedAt")
    void preUpdateRefreshesUpdatedAt() {
        UserSubscription subscription = new UserSubscription();
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusHours(1);
        subscription.setUpdatedAt(originalUpdatedAt);

        subscription.preUpdate();

        assertThat(subscription.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("setters and getters store all fields")
    void settersAndGettersStoreFields() {
        UserSubscription subscription = new UserSubscription();
        LocalDateTime startDate = LocalDateTime.of(2026, 3, 9, 10, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 4, 8, 10, 0);

        subscription.setId(1L);
        subscription.setUserId(2L);
        subscription.setPlanType(PlanType.MONTHLY);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        assertThat(subscription.getId()).isEqualTo(1L);
        assertThat(subscription.getUserId()).isEqualTo(2L);
        assertThat(subscription.getPlanType()).isEqualTo(PlanType.MONTHLY);
        assertThat(subscription.getStartDate()).isEqualTo(startDate);
        assertThat(subscription.getEndDate()).isEqualTo(endDate);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }
}
