package com.devs.simplicity.poke_go_friends.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Configuration for Circuit Breaker used in rate limiting.
 * 
 * This configuration sets up a circuit breaker to protect against Redis failures
 * and provide graceful degradation to in-memory rate limiting.
 */
@Configuration
@ConfigurationProperties(prefix = "app.circuit-breaker.rate-limiter")
@Slf4j
public class CircuitBreakerConfig {
    
    private int failureRateThreshold = 50;
    private int minimumNumberOfCalls = 10;
    private int waitDurationInOpenStateSeconds = 30;
    private int slidingWindowSize = 20;
    private int permittedNumberOfCallsInHalfOpenState = 5;
    
    /**
     * Creates a Circuit Breaker specifically for the rate limiter service.
     * 
     * @return Configured CircuitBreaker instance
     */
    @Bean
    @Primary
    public CircuitBreaker rateLimiterCircuitBreaker() {
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationInOpenStateSeconds))
                .slidingWindowSize(slidingWindowSize)
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        
        CircuitBreaker circuitBreaker = CircuitBreaker.of("rateLimiter", config);
        
        // Add event listeners for monitoring
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.warn("Rate limiter circuit breaker state transition: {} -> {}", 
                            event.getStateTransition().getFromState(), 
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event ->
                    log.error("Rate limiter circuit breaker failure rate exceeded: {}%", 
                            event.getFailureRate()))
                .onCallNotPermitted(event ->
                    log.warn("Rate limiter circuit breaker call not permitted - circuit is OPEN"))
                .onError(event ->
                    log.debug("Rate limiter circuit breaker recorded error: {}", 
                            event.getThrowable().getMessage()));
        
        log.info("Rate limiter circuit breaker configured with failure rate threshold: {}%, " +
                "minimum calls: {}, wait duration: {}s", 
                failureRateThreshold, minimumNumberOfCalls, waitDurationInOpenStateSeconds);
        
        return circuitBreaker;
    }
    
    // Getters and setters for configuration properties
    
    public int getFailureRateThreshold() {
        return failureRateThreshold;
    }
    
    public void setFailureRateThreshold(int failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }
    
    public int getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }
    
    public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
        this.minimumNumberOfCalls = minimumNumberOfCalls;
    }
    
    public int getWaitDurationInOpenStateSeconds() {
        return waitDurationInOpenStateSeconds;
    }
    
    public void setWaitDurationInOpenStateSeconds(int waitDurationInOpenStateSeconds) {
        this.waitDurationInOpenStateSeconds = waitDurationInOpenStateSeconds;
    }
    
    public int getSlidingWindowSize() {
        return slidingWindowSize;
    }
    
    public void setSlidingWindowSize(int slidingWindowSize) {
        this.slidingWindowSize = slidingWindowSize;
    }
    
    public int getPermittedNumberOfCallsInHalfOpenState() {
        return permittedNumberOfCallsInHalfOpenState;
    }
    
    public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
        this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
    }
}
