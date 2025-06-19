package com.devs.simplicity.poke_go_friends.metrics;

import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ApplicationMetricsService {

    private final MeterRegistry meterRegistry;
    private final FriendCodeRepository friendCodeRepository;
    private Counter submissionsCounter;
    private Counter rateLimitCounter;

    public ApplicationMetricsService(MeterRegistry meterRegistry, FriendCodeRepository friendCodeRepository) {
        this.meterRegistry = meterRegistry;
        this.friendCodeRepository = friendCodeRepository;
        initCounters();
    }

    private void initCounters() {
        submissionsCounter = Counter.builder("submissions.total")
                .description("Total number of friend code submissions")
                .register(meterRegistry);

        rateLimitCounter = Counter.builder("rate.limit.hits")
                .description("Total number of rate limit hits")
                .register(meterRegistry);
    }

    @PostConstruct
    public void initGauges() {
        Gauge.builder("friend.codes.active", this, service -> service.getActiveFriendCodesCount())
                .description("Total number of active friend codes")
                .register(meterRegistry);
    }

    public void incrementSubmissions() {
        submissionsCounter.increment();
    }

    public void incrementRateLimitHits() {
        rateLimitCounter.increment();
    }

    public long getActiveFriendCodesCount() {
        return friendCodeRepository.countActiveFriendCodes(LocalDateTime.now());
    }
}
