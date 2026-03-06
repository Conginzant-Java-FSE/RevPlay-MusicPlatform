package com.revplay.musicplatform.premium.service.impl;

import com.revplay.musicplatform.exception.BadRequestException;
import com.revplay.musicplatform.premium.dto.PremiumStatusResponse;
import com.revplay.musicplatform.premium.entity.SubscriptionPayment;
import com.revplay.musicplatform.premium.entity.UserSubscription;
import com.revplay.musicplatform.premium.enums.PlanType;
import com.revplay.musicplatform.premium.enums.SubscriptionStatus;
import com.revplay.musicplatform.premium.repository.SubscriptionPaymentRepository;
import com.revplay.musicplatform.premium.repository.UserSubscriptionRepository;
import com.revplay.musicplatform.user.entity.User;
import com.revplay.musicplatform.user.repository.UserRepository;
import com.revplay.musicplatform.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    private static final Long USER_ID = 10L;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionServiceImpl service;

    @Test
    @DisplayName("isUserPremium active future end date returns true")
    void isUserPremium_activeFuture_true() {
        UserSubscription sub = activeSub(LocalDateTime.now().plusDays(1));
        when(userSubscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        boolean actual = service.isUserPremium(USER_ID);

        assertThat(actual).isTrue();
        verify(userSubscriptionRepository, never()).save(any(UserSubscription.class));
    }

    @Test
    @DisplayName("isUserPremium active past end date auto expires and returns false")
    void isUserPremium_activePast_expiresFalse() {
        UserSubscription sub = activeSub(LocalDateTime.now().minusSeconds(1));
        when(userSubscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        boolean actual = service.isUserPremium(USER_ID);

        assertThat(actual).isFalse();
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        verify(userSubscriptionRepository).save(sub);
    }

    @Test
    @DisplayName("isUserPremium active endDate equals now expires and returns false")
    void isUserPremium_boundaryNow_expiresFalse() {
        UserSubscription sub = activeSub(LocalDateTime.now());
        when(userSubscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        boolean actual = service.isUserPremium(USER_ID);

        assertThat(actual).isFalse();
        verify(userSubscriptionRepository).save(sub);
    }

    @Test
    @DisplayName("isUserPremium no active subscription returns false")
    void isUserPremium_noActive_false() {
        when(userSubscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        boolean actual = service.isUserPremium(USER_ID);

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("isUserPremium invalid user ids throw BadRequestException")
    void isUserPremium_invalidUserId_throws() {
        assertThatThrownBy(() -> service.isUserPremium(null)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.isUserPremium(0L)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.isUserPremium(-1L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("upgradeToPremium new monthly creates subscription and payment")
    void upgradeToPremium_newMonthly_creates() {
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> {
            UserSubscription s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(77L);
            }
            return s;
        });
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser(USER_ID)));

        service.upgradeToPremium(USER_ID, "MONTHLY");

        ArgumentCaptor<UserSubscription> subCaptor = ArgumentCaptor.forClass(UserSubscription.class);
        verify(userSubscriptionRepository).save(subCaptor.capture());
        UserSubscription savedSub = subCaptor.getValue();
        assertThat(savedSub.getPlanType()).isEqualTo(PlanType.MONTHLY);
        assertThat(savedSub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        ArgumentCaptor<SubscriptionPayment> paymentCaptor = ArgumentCaptor.forClass(SubscriptionPayment.class);
        verify(subscriptionPaymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getAmount()).isEqualTo(199.0);
        assertThat(paymentCaptor.getValue().getCurrency()).isEqualTo("INR");
    }

    @Test
    @DisplayName("upgradeToPremium new yearly sets yearly amount")
    void upgradeToPremium_newYearly_amountYearly() {
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> {
            UserSubscription s = inv.getArgument(0);
            s.setId(88L);
            return s;
        });

        service.upgradeToPremium(USER_ID, "YEARLY");

        ArgumentCaptor<SubscriptionPayment> paymentCaptor = ArgumentCaptor.forClass(SubscriptionPayment.class);
        verify(subscriptionPaymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getAmount()).isEqualTo(1499.0);
    }

    @Test
    @DisplayName("upgradeToPremium active future extends from current end date")
    void upgradeToPremium_activeFuture_extendsFromEndDate() {
        LocalDateTime oldEnd = LocalDateTime.now().plusDays(10);
        UserSubscription active = activeSub(oldEnd);
        active.setId(1L);
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of(active));
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        service.upgradeToPremium(USER_ID, "MONTHLY");

        assertThat(active.getEndDate()).isAfter(oldEnd.plusDays(29));
    }

    @Test
    @DisplayName("upgradeToPremium active past extends from now")
    void upgradeToPremium_activePast_extendsFromNow() {
        UserSubscription active = activeSub(LocalDateTime.now().minusDays(1));
        active.setId(2L);
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of(active));
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        service.upgradeToPremium(USER_ID, "MONTHLY");

        assertThat(active.getEndDate()).isAfter(LocalDateTime.now().plusDays(29));
    }

    @Test
    @DisplayName("upgradeToPremium multiple active keeps first and cancels rest")
    void upgradeToPremium_multipleActive_cancelsExtras() {
        UserSubscription first = activeSub(LocalDateTime.now().plusDays(1));
        first.setId(3L);
        UserSubscription second = activeSub(LocalDateTime.now().plusDays(2));
        second.setId(4L);
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of(first, second));
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        service.upgradeToPremium(USER_ID, "MONTHLY");

        assertThat(second.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        verify(subscriptionPaymentRepository, times(1)).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("upgradeToPremium email failure is swallowed")
    void upgradeToPremium_emailFailure_swallowed() {
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> {
            UserSubscription s = inv.getArgument(0);
            s.setId(55L);
            return s;
        });
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser(USER_ID)));
        doThrow(new RuntimeException("smtp down")).when(emailService).sendPremiumSubscriptionEmail(any(), any(), any());

        service.upgradeToPremium(USER_ID, "MONTHLY");

        verify(subscriptionPaymentRepository).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("upgradeToPremium user missing in repo completes without email")
    void upgradeToPremium_userMissing_completes() {
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> {
            UserSubscription s = inv.getArgument(0);
            s.setId(66L);
            return s;
        });
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        service.upgradeToPremium(USER_ID, "MONTHLY");

        verify(subscriptionPaymentRepository).save(any(SubscriptionPayment.class));
        verify(emailService, never()).sendPremiumSubscriptionEmail(any(), any(), any());
    }

    @Test
    @DisplayName("upgradeToPremium invalid planType values throw BadRequestException")
    void upgradeToPremium_invalidPlan_throws() {
        assertThatThrownBy(() -> service.upgradeToPremium(USER_ID, null)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.upgradeToPremium(USER_ID, "   ")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.upgradeToPremium(USER_ID, "WEEKLY")).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("upgradeToPremium lowercase and whitespace planType are accepted")
    void upgradeToPremium_normalizedPlanType_accepted() {
        when(userSubscriptionRepository.findByUserIdAndStatus(USER_ID, SubscriptionStatus.ACTIVE)).thenReturn(List.of());
        when(userSubscriptionRepository.save(any(UserSubscription.class))).thenAnswer(inv -> {
            UserSubscription s = inv.getArgument(0);
            s.setId(90L);
            return s;
        });

        service.upgradeToPremium(USER_ID, "monthly");
        service.upgradeToPremium(USER_ID, "  MONTHLY  ");

        verify(subscriptionPaymentRepository, times(2)).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("getPremiumStatus active returns premium true with expiry")
    void getPremiumStatus_active_true() {
        UserSubscription sub = activeSub(LocalDateTime.now().plusDays(5));
        when(userSubscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        PremiumStatusResponse response = service.getPremiumStatus(USER_ID);

        assertThat(response.isPremium()).isTrue();
        assertThat(response.expiryDate()).isNotNull();
    }

    @Test
    @DisplayName("getPremiumStatus no sub returns false with null expiry")
    void getPremiumStatus_noSub_falseNull() {
        when(userSubscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        PremiumStatusResponse response = service.getPremiumStatus(USER_ID);

        assertThat(response.isPremium()).isFalse();
        assertThat(response.expiryDate()).isNull();
    }

    @Test
    @DisplayName("getPremiumStatus expired path returns false and saves expired")
    void getPremiumStatus_expired_savesExpired() {
        UserSubscription sub = activeSub(LocalDateTime.now().minusSeconds(5));
        when(userSubscriptionRepository.findFirstByUserIdAndStatusOrderByEndDateDesc(USER_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(sub));

        PremiumStatusResponse response = service.getPremiumStatus(USER_ID);

        assertThat(response.isPremium()).isFalse();
        assertThat(response.expiryDate()).isNull();
        verify(userSubscriptionRepository).save(sub);
    }

    @Test
    @DisplayName("getPremiumStatus invalid user id propagates BadRequestException")
    void getPremiumStatus_invalidUser_throws() {
        assertThatThrownBy(() -> service.getPremiumStatus(0L)).isInstanceOf(BadRequestException.class);
    }

    private UserSubscription activeSub(LocalDateTime endDate) {
        UserSubscription sub = new UserSubscription();
        sub.setUserId(USER_ID);
        sub.setPlanType(PlanType.MONTHLY);
        sub.setStartDate(LocalDateTime.now().minusDays(1));
        sub.setEndDate(endDate);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        return sub;
    }

    private User testUser(Long id) {
        User user = new User();
        user.setUserId(id);
        user.setEmail("user@mail.test");
        user.setUsername("user");
        return user;
    }
}
