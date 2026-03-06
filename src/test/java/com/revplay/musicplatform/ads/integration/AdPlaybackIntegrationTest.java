package com.revplay.musicplatform.ads.integration;

import com.revplay.musicplatform.ads.entity.Ad;
import com.revplay.musicplatform.ads.repository.AdImpressionRepository;
import com.revplay.musicplatform.ads.repository.AdRepository;
import com.revplay.musicplatform.ads.repository.UserAdPlaybackStateRepository;
import com.revplay.musicplatform.ads.service.AdService;
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

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class AdPlaybackIntegrationTest {

    private static final Long NON_PREMIUM_USER_ID = 701L;
    private static final Long PREMIUM_USER_ID = 702L;

    private final AdService adService;
    private final SubscriptionService subscriptionService;
    private final AdRepository adRepository;
    private final AdImpressionRepository adImpressionRepository;
    private final UserAdPlaybackStateRepository userAdPlaybackStateRepository;
    private final JdbcTemplate jdbcTemplate;

    @MockBean
    private EmailService emailService;

    @Autowired
    AdPlaybackIntegrationTest(
            AdService adService,
            SubscriptionService subscriptionService,
            AdRepository adRepository,
            AdImpressionRepository adImpressionRepository,
            UserAdPlaybackStateRepository userAdPlaybackStateRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.adService = adService;
        this.subscriptionService = subscriptionService;
        this.adRepository = adRepository;
        this.adImpressionRepository = adImpressionRepository;
        this.userAdPlaybackStateRepository = userAdPlaybackStateRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        adImpressionRepository.deleteAll();
        userAdPlaybackStateRepository.deleteAll();
        adRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM user_subscriptions");
        jdbcTemplate.update("DELETE FROM subscription_payments");
        jdbcTemplate.update("DELETE FROM users");

        insertUser(NON_PREMIUM_USER_ID);
        insertUser(PREMIUM_USER_ID);
        createActiveAd();
    }

    @Test
    @DisplayName("non premium user gets ad on third song only")
    void nonPremium_adCadenceEveryThirdSong() {
        Ad first = adService.getNextAd(NON_PREMIUM_USER_ID, 1L);
        Ad second = adService.getNextAd(NON_PREMIUM_USER_ID, 2L);
        Ad third = adService.getNextAd(NON_PREMIUM_USER_ID, 3L);

        assertThat(first).isNull();
        assertThat(second).isNull();
        assertThat(third).isNotNull();
        assertThat(adImpressionRepository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("premium user receives no ads")
    void premiumUser_noAds() {
        subscriptionService.upgradeToPremium(PREMIUM_USER_ID, "MONTHLY");

        Ad first = adService.getNextAd(PREMIUM_USER_ID, 10L);
        Ad second = adService.getNextAd(PREMIUM_USER_ID, 11L);
        Ad third = adService.getNextAd(PREMIUM_USER_ID, 12L);

        assertThat(first).isNull();
        assertThat(second).isNull();
        assertThat(third).isNull();
        assertThat(adImpressionRepository.count()).isZero();
    }

    @Test
    @DisplayName("impressions increase only when ads are shown")
    void impressionsIncrease_onlyOnShownAds() {
        adService.getNextAd(NON_PREMIUM_USER_ID, 101L);
        adService.getNextAd(NON_PREMIUM_USER_ID, 102L);
        assertThat(adImpressionRepository.count()).isZero();

        adService.getNextAd(NON_PREMIUM_USER_ID, 103L);
        assertThat(adImpressionRepository.count()).isEqualTo(1L);
    }

    private void createActiveAd() {
        Ad ad = new Ad();
        ad.setTitle("Integration Ad");
        ad.setMediaUrl("/uploads/ads/integration.mp3");
        ad.setDurationSeconds(15);
        ad.setIsActive(true);
        ad.setStartDate(LocalDateTime.now().minusDays(1));
        ad.setEndDate(LocalDateTime.now().plusDays(2));
        adRepository.save(ad);
    }

    private void insertUser(Long userId) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                "INSERT INTO users (user_id, email, username, password_hash, role, is_active, created_at, updated_at, email_verified, version) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId,
                "ads" + userId + "@mail.test",
                "ads" + userId,
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
