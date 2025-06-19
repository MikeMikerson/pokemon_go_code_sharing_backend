package com.devs.simplicity.poke_go_friends.metrics;

import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationMetricsServiceTest {

    private MeterRegistry meterRegistry;
    private ApplicationMetricsService metricsService;

    @Mock
    private FriendCodeRepository friendCodeRepository;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new ApplicationMetricsService(meterRegistry, friendCodeRepository);
    }

    @Test
    void incrementSubmissions_shouldIncrementCounter() {
        metricsService.incrementSubmissions();
        metricsService.incrementSubmissions();

        Counter submissionsCounter = meterRegistry.find("submissions.total").counter();
        assertEquals(2, submissionsCounter.count());
    }

    @Test
    void incrementRateLimitHits_shouldIncrementCounter() {
        metricsService.incrementRateLimitHits();

        Counter rateLimitCounter = meterRegistry.find("rate.limit.hits").counter();
        assertEquals(1, rateLimitCounter.count());
    }

    @Test
    void activeFriendCodesGauge_shouldReturnCountFromRepository() {
        when(friendCodeRepository.countActiveFriendCodes(any(LocalDateTime.class))).thenReturn(42L);
        metricsService.initGauges();

        Gauge gauge = meterRegistry.find("friend.codes.active").gauge();
        assertEquals(42, gauge.value());
    }
}
