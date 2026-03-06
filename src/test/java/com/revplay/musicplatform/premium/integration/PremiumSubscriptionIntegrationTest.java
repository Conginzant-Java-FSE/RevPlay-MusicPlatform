package com.revplay.musicplatform.premium.integration;

import com.revplay.musicplatform.premium.dto.PremiumStatusResponse;
import com.revplay.musicplatform.premium.entity.UserSubscription;
import com.revplay.musicplatform.premium.enums.SubscriptionStatus;
import com.revplay.musicplatform.premium.repository.SubscriptionPaymentRepository;
import com.revplay.musicplatform.premium.repository.UserSubscriptionRepository;
import com.revplay.musicplatform.premium.service.SubscriptionService;
import com.revplay.musicplatform.user.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class PremiumSubscriptionIntegrationTest {

    private static final Long USER_ID = 501L;

    private final SubscriptionService subscriptionService;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final JdbcTemplate jdbcTemplate;

    @MockBean
    private EmailService emailService;

    @Autowired
    PremiumSubscriptionIntegrationTest(
            SubscriptionService subscriptionService,
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionPaymentRepository subscriptionPaymentRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.subscriptionService = subscriptionService;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPaymentRepository = subscriptionPaymentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        subscriptionPaymentRepository.deleteAll();
        userSubscriptionRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM users");
        insertUser(USER_ID);
    }

    @Test
    @DisplayName("upgrade monthly makes user premium with around thirty day expiry")
    void upgradeMonthly_premiumTrue_expiryAroundThirtyDays() {
        LocalDateTime before = LocalDateTime.now().plusDays(29);
        subscriptionService.upgradeToPremium(USER_ID, "MONTHLY");

        boolean premium = subscriptionService.isUserPremium(USER_ID);
        PremiumStatusResponse status = subscriptionService.getPremiumStatus(USER_ID);

        assertThat(premium).isTrue();
        assertThat(status.isPremium()).isTrue();
        assertThat(status.expiryDate()).isAfter(before);
    }

    @Test
    @DisplayName("upgrade yearly makes user premium with around three sixty five day expiry")
    void upgradeYearly_premiumTrue_expiryAroundYear() {
        LocalDateTime before = LocalDateTime.now().plusDays(364);
        subscriptionService.upgradeToPremium(USER_ID, "YEARLY");

        PremiumStatusResponse status = subscriptionService.getPremiumStatus(USER_ID);

        assertThat(status.isPremium()).isTrue();
        assertThat(status.expiryDate()).isAfter(before);
    }

    @Test
    @DisplayName("upgrade twice extends end date further")
    void upgradeTwice_extendsEndDate() {
        subscriptionService.upgradeToPremium(USER_ID, "MONTHLY");
        LocalDateTime firstEnd = activeSubscription().getEndDate();

        subscriptionService.upgradeToPremium(USER_ID, "MONTHLY");
        LocalDateTime secondEnd = activeSubscription().getEndDate();

        assertThat(secondEnd).isAfter(firstEnd);
    }

    @Test
    @DisplayName("expired end date flips status to EXPIRED and premium false")
    void expiredSubscription_autoExpires() {
        subscriptionService.upgradeToPremium(USER_ID, "MONTHLY");
        UserSubscription active = activeSubscription();
        active.setEndDate(LocalDateTime.now().minusSeconds(1));
        active.setStatus(SubscriptionStatus.ACTIVE);
        userSubscriptionRepository.save(active);

        boolean premium = subscriptionService.isUserPremium(USER_ID);
        UserSubscription updated = userSubscriptionRepository.findById(active.getId()).orElseThrow();

        assertThat(premium).isFalse();
        assertThat(updated.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    @DisplayName("premium status endpoint shape reflects no sub then active then expired")
    void getPremiumStatus_reflectsStateTransitions() {
        PremiumStatusResponse initial = subscriptionService.getPremiumStatus(USER_ID);
        assertThat(initial.isPremium()).isFalse();
        assertThat(initial.expiryDate()).isNull();

        subscriptionService.upgradeToPremium(USER_ID, "MONTHLY");
        PremiumStatusResponse active = subscriptionService.getPremiumStatus(USER_ID);
        assertThat(active.isPremium()).isTrue();
        assertThat(active.expiryDate()).isNotNull();

        UserSubscription subscription = activeSubscription();
        subscription.setEndDate(LocalDateTime.now().minusMinutes(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        userSubscriptionRepository.save(subscription);

        PremiumStatusResponse expired = subscriptionService.getPremiumStatus(USER_ID);
        assertThat(expired.isPremium()).isFalse();
        assertThat(expired.expiryDate()).isNull();
    }

    private UserSubscription activeSubscription() {
        return userSubscriptionRepository
                .findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE)
                .orElseThrow();
    }

    private void insertUser(Long userId) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                "INSERT INTO users (user_id, email, username, password_hash, role, is_active, created_at, updated_at, email_verified, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId,
                "premium" + userId + "@mail.test",
                "premium" + userId,
                "hash",
                "LISTENER",
                true,
                now,
                now,
                true,
                0L
        );
    }
}
