package com.devs.simplicity.poke_go_friends.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Circuit breaker-protected rate limiter that provides graceful degradation.
 * 
 * This implementation wraps the Redis-based rate limiter with a circuit breaker
 * and falls back to an in-memory rate limiter when Redis is unavailable.
 * 
 * The circuit breaker monitors Redis operations and:
 * - Opens when failure rate exceeds the threshold
 * - Falls back to in-memory rate limiting when open
 * - Attempts to recover by transitioning to half-open state
 */
@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class CircuitBreakerRateLimiter implements RateLimiter {
    
    private final RedisRateLimiter redisRateLimiter;
    private final InMemoryRateLimiter fallbackRateLimiter;
    private final CircuitBreaker circuitBreaker;
    
    /**
     * Checks if a request is allowed using circuit breaker protection.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    @Override
    public boolean isAllowed(String key) {
        try {
            // Execute Redis rate limiting with circuit breaker protection
            Supplier<Boolean> rateLimitSupplier = () -> redisRateLimiter.isAllowed(key);
            return circuitBreaker.executeSupplier(rateLimitSupplier);
            
        } catch (CallNotPermittedException e) {
            // Circuit breaker is open - fall back to in-memory rate limiting
            log.warn("Circuit breaker is open, falling back to in-memory rate limiting for key: {}", key);
            return fallbackRateLimiter.isAllowed(key);
            
        } catch (Exception e) {
            // Unexpected error - fall back to in-memory rate limiting
            log.error("Unexpected error in rate limiting, falling back to in-memory for key '{}': {}", 
                     key, e.getMessage(), e);
            return fallbackRateLimiter.isAllowed(key);
        }
    }
    
    /**
     * Checks if a request is allowed with custom limit and window size.
     * 
     * This method is specific to RedisRateLimiter functionality.
     * When circuit breaker is open, it falls back to the basic isAllowed method.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @param limit The maximum number of requests allowed in the window
     * @param windowSizeMs The sliding window size in milliseconds
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    public boolean isAllowed(String key, int limit, long windowSizeMs) {
        try {
            // Execute Redis rate limiting with circuit breaker protection
            Supplier<Boolean> rateLimitSupplier = () -> redisRateLimiter.isAllowed(key, limit, windowSizeMs);
            return circuitBreaker.executeSupplier(rateLimitSupplier);
            
        } catch (CallNotPermittedException e) {
            // Circuit breaker is open - fall back to in-memory rate limiting
            log.warn("Circuit breaker is open, falling back to in-memory rate limiting for key: {}", key);
            return fallbackRateLimiter.isAllowed(key);
            
        } catch (Exception e) {
            // Unexpected error - fall back to in-memory rate limiting
            log.error("Unexpected error in rate limiting, falling back to in-memory for key '{}': {}", 
                     key, e.getMessage(), e);
            return fallbackRateLimiter.isAllowed(key);
        }
    }
    
    /**
     * Gets the current usage count for a given key.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @return The current number of requests in the sliding window
     */
    public long getCurrentUsage(String key) {
        try {
            // Execute Redis operation with circuit breaker protection
            Supplier<Long> usageSupplier = () -> redisRateLimiter.getCurrentUsage(key);
            return circuitBreaker.executeSupplier(usageSupplier);
            
        } catch (CallNotPermittedException e) {
            // Circuit breaker is open - fall back to in-memory data
            log.debug("Circuit breaker is open, falling back to in-memory usage count for key: {}", key);
            return fallbackRateLimiter.getCurrentUsage(key);
            
        } catch (Exception e) {
            // Unexpected error - fall back to in-memory data
            log.error("Error getting usage count, falling back to in-memory for key '{}': {}", 
                     key, e.getMessage(), e);
            return fallbackRateLimiter.getCurrentUsage(key);
        }
    }
    
    /**
     * Gets the current usage count for a given key within a custom window.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @param windowSizeMs The sliding window size in milliseconds
     * @return The current number of requests in the sliding window
     */
    public long getCurrentUsage(String key, long windowSizeMs) {
        try {
            // Execute Redis operation with circuit breaker protection
            Supplier<Long> usageSupplier = () -> redisRateLimiter.getCurrentUsage(key, windowSizeMs);
            return circuitBreaker.executeSupplier(usageSupplier);
            
        } catch (CallNotPermittedException e) {
            // Circuit breaker is open - fall back to in-memory data
            log.debug("Circuit breaker is open, falling back to in-memory usage count for key: {}", key);
            return fallbackRateLimiter.getCurrentUsage(key);
            
        } catch (Exception e) {
            // Unexpected error - fall back to in-memory data
            log.error("Error getting usage count, falling back to in-memory for key '{}': {}", 
                     key, e.getMessage(), e);
            return fallbackRateLimiter.getCurrentUsage(key);
        }
    }
    
    /**
     * Clears the rate limit data for a specific key.
     * 
     * @param key The unique identifier for the rate limit bucket
     */
    public void clearRateLimit(String key) {
        try {
            // Execute Redis operation with circuit breaker protection
            Supplier<Void> clearSupplier = () -> {
                redisRateLimiter.clearRateLimit(key);
                return null;
            };
            circuitBreaker.executeSupplier(clearSupplier);
            
        } catch (CallNotPermittedException e) {
            // Circuit breaker is open - clear from in-memory store
            log.debug("Circuit breaker is open, clearing in-memory rate limit for key: {}", key);
            fallbackRateLimiter.clearRateLimit(key);
            
        } catch (Exception e) {
            // Unexpected error - clear from in-memory store
            log.error("Error clearing rate limit, falling back to in-memory for key '{}': {}", 
                     key, e.getMessage(), e);
            fallbackRateLimiter.clearRateLimit(key);
        }
    }
    
    /**
     * Gets the current circuit breaker state.
     * 
     * @return The current state of the circuit breaker
     */
    public CircuitBreaker.State getCircuitBreakerState() {
        return circuitBreaker.getState();
    }
    
    /**
     * Gets circuit breaker metrics for monitoring.
     * 
     * @return Circuit breaker metrics
     */
    public CircuitBreaker.Metrics getCircuitBreakerMetrics() {
        return circuitBreaker.getMetrics();
    }
    
    /**
     * Cleans up rate limiting data in both Redis and in-memory stores.
     * Since this is a maintenance operation, we don't protect it with the circuit breaker.
     */
    public void cleanupRateLimitData() {
        // Clean up in-memory data (always available)
        fallbackRateLimiter.cleanupRateLimitData();
        
        // Note: Redis handles its own cleanup via TTL, so no explicit cleanup needed
        log.debug("Rate limiting cleanup completed for both Redis and in-memory stores");
    }
}
