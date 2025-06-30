package com.devs.simplicity.poke_go_friends.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RateLimitConfig.
 */
@DisplayName("RateLimitConfig Tests")
class RateLimitConfigTest {

    private RateLimitConfig config;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
    }

    @Test
    @DisplayName("Should have default values")
    void shouldHaveDefaultValues() {
        assertThat(config.getSubmissionsPerHourPerIp()).isEqualTo(5);
        assertThat(config.getSubmissionsPerDayPerUser()).isEqualTo(10);
        assertThat(config.getUpdatesPerHourPerIp()).isEqualTo(10);
        assertThat(config.getSearchesPerMinutePerIp()).isEqualTo(30);
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.getCleanupIntervalMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should allow setting custom values")
    void shouldAllowSettingCustomValues() {
        config.setSubmissionsPerHourPerIp(10);
        config.setSubmissionsPerDayPerUser(20);
        config.setUpdatesPerHourPerIp(15);
        config.setSearchesPerMinutePerIp(50);
        config.setEnabled(false);
        config.setCleanupIntervalMinutes(120);

        assertThat(config.getSubmissionsPerHourPerIp()).isEqualTo(10);
        assertThat(config.getSubmissionsPerDayPerUser()).isEqualTo(20);
        assertThat(config.getUpdatesPerHourPerIp()).isEqualTo(15);
        assertThat(config.getSearchesPerMinutePerIp()).isEqualTo(50);
        assertThat(config.isEnabled()).isFalse();
        assertThat(config.getCleanupIntervalMinutes()).isEqualTo(120);
    }
}
