package com.revplay.musicplatform.security.service.impl;

import com.revplay.musicplatform.user.exception.AuthValidationException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class InMemoryRateLimiterServiceImplTest {

    private static final String TEST_KEY = "test-key";
    private static final String ERROR_MESSAGE = "Too many requests";
    private static final int MAX_REQUESTS = 3;
    private static final int WINDOW_SECONDS = 1;

    private InMemoryRateLimiterServiceImpl rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new InMemoryRateLimiterServiceImpl();
    }

    @Test
    @DisplayName("calls < maxRequests: no exception")
    void ensureWithinLimit_BelowLimit_NoException() {
        assertThatCode(() -> {
            rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE);
            rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("calls = maxRequests: no exception on Nth call")
    void ensureWithinLimit_AtLimit_NoException() {
        assertThatCode(() -> {
            for (int i = 0; i < MAX_REQUESTS; i++) {
                rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("calls = maxRequests + 1: exception on (N+1)th call")
    void ensureWithinLimit_ExceedLimit_ThrowsException() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE);
        }

        assertThatThrownBy(
                () -> rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE))
                .isInstanceOf(AuthValidationException.class)
                .hasMessage(ERROR_MESSAGE);
    }

    @Test
    @DisplayName("different keys are independent")
    void ensureWithinLimit_IndependentKeys() {
        String otherKey = "other-key";
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE);
        }

        // TEST_KEY is at limit, but otherKey should be fine
        assertThatCode(
                () -> rateLimiterService.ensureWithinLimit(otherKey, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("null key throws NullPointerException in current implementation")
    void ensureWithinLimit_NullKey() {
        assertThatThrownBy(() -> rateLimiterService.ensureWithinLimit(null, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("window reset: limit resets after window expiry")
    @Timeout(5)
    void ensureWithinLimit_WindowReset() throws InterruptedException {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE);
        }

        // Wait for window to expire
        Thread.sleep(1100);

        assertThatCode(
                () -> rateLimiterService.ensureWithinLimit(TEST_KEY, MAX_REQUESTS, WINDOW_SECONDS, ERROR_MESSAGE))
                .doesNotThrowAnyException();
    }
}
