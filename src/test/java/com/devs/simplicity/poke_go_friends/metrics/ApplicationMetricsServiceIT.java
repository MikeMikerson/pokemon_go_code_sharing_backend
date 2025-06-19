package com.devs.simplicity.poke_go_friends.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationMetricsServiceIT {

    @Autowired
    private ApplicationMetricsService metricsService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void metricsService_shouldBeInjectable() {
        assertNotNull(metricsService);
    }

    @Test
    void incrementSubmissions_shouldWorkInSpringContext() {
        metricsService.incrementSubmissions();

        Counter counter = meterRegistry.find("submissions.total").counter();
        assertNotNull(counter);
        assertTrue(counter.count() >= 1);
    }

    @Test
    void incrementRateLimitHits_shouldWorkInSpringContext() {
        metricsService.incrementRateLimitHits();

        Counter counter = meterRegistry.find("rate.limit.hits").counter();
        assertNotNull(counter);
        assertTrue(counter.count() >= 1);
    }

    @Test
    void activeFriendCodesGauge_shouldBeRegistered() {
        Gauge gauge = meterRegistry.find("friend.codes.active").gauge();
        assertNotNull(gauge);
        assertTrue(gauge.value() >= 0);
    }
}
