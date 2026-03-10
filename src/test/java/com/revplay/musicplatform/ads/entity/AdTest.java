package com.revplay.musicplatform.ads.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AdTest {

    private static final String PRE_PERSIST = "prePersist";
    private static final String PRE_UPDATE = "preUpdate";

    @Test
    @DisplayName("prePersist sets createdAt, updatedAt and default active flag")
    void prePersistSetsAuditFieldsAndDefaultActive() throws Exception {
        Ad ad = new Ad();
        ad.setIsActive(null);

        invokeLifecycle(ad, PRE_PERSIST);

        assertThat(ad.getCreatedAt()).isNotNull();
        assertThat(ad.getUpdatedAt()).isNotNull();
        assertThat(ad.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("prePersist keeps explicit active flag value")
    void prePersistKeepsExplicitActiveFlag() throws Exception {
        Ad ad = new Ad();
        ad.setIsActive(false);

        invokeLifecycle(ad, PRE_PERSIST);

        assertThat(ad.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("preUpdate refreshes updatedAt timestamp")
    void preUpdateRefreshesUpdatedAt() throws Exception {
        Ad ad = new Ad();
        ad.setUpdatedAt(LocalDateTime.now().minusHours(1));

        invokeLifecycle(ad, PRE_UPDATE);

        assertThat(ad.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    private void invokeLifecycle(Ad ad, String methodName) throws Exception {
        Method method = Ad.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(ad);
    }
}
