package com.revplay.musicplatform.premium.integration;

import com.revplay.musicplatform.ads.entity.Ad;
import com.revplay.musicplatform.ads.service.impl.AdServiceImpl;
import com.revplay.musicplatform.premium.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PremiumAwareAdServiceTest {

    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private AdServiceImpl delegate;

    @InjectMocks
    private PremiumAwareAdService service;

    @Test
    @DisplayName("premium user returns null and does not delegate")
    void getNextAd_premium_returnsNullNoDelegate() {
        when(subscriptionService.isUserPremium(1L)).thenReturn(true);

        Ad ad = service.getNextAd(1L, 10L);

        assertThat(ad).isNull();
        verify(delegate, never()).getNextAd(1L, 10L);
    }

    @Test
    @DisplayName("non premium delegates and returns ad")
    void getNextAd_nonPremium_delegates() {
        Ad ad = new Ad();
        ad.setId(7L);
        when(subscriptionService.isUserPremium(1L)).thenReturn(false);
        when(delegate.getNextAd(1L, 10L)).thenReturn(ad);

        Ad result = service.getNextAd(1L, 10L);

        assertThat(result).isEqualTo(ad);
        verify(delegate).getNextAd(1L, 10L);
    }

    @Test
    @DisplayName("non premium and delegate returns null propagates null")
    void getNextAd_nonPremium_delegateNull_returnsNull() {
        when(subscriptionService.isUserPremium(1L)).thenReturn(false);
        when(delegate.getNextAd(1L, 10L)).thenReturn(null);

        Ad result = service.getNextAd(1L, 10L);

        assertThat(result).isNull();
    }
}
