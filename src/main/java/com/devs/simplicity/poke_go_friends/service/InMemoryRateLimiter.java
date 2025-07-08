package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RateLimitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter implementation using ConcurrentHashMap.
 * 
 * This implementation serves as a fallback when Redis is unavailable.
 * It uses the original ConcurrentHashMap-based approach for rate limiting
 * with sliding window logic based on timestamps.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InMemoryRateLimiter implements RateLimiter {
    
    private final RateLimitConfig rateLimitConfig;
    private final ConcurrentHashMap<String, RateLimitData> rateLimitMap = new ConcurrentHashMap<>();
    
    /**
     * Data structure to hold rate limiting information.
     */
    private static class RateLimitData {
        private final LocalDateTime firstRequest;
        private int requestCount;
        
        public RateLimitData() {
            this.firstRequest = LocalDateTime.now();
            this.requestCount = 1;
        }
        
        public LocalDateTime getFirstRequest() {
            return firstRequest;
        }
        
        public int getRequestCount() {
            return requestCount;
        }
        
        public void incrementCount() {
            this.requestCount++;
        }
    }
    
    /**
     * Checks if a request is allowed based on the configured rate limits.
     * 
     * This method determines the appropriate limit and window based on the key pattern:
     * - IP keys (ip:*:submission): uses hourly limits
     * - User keys (user:*:submission): uses daily limits
     * 
     * @param key The unique identifier for the rate limit bucket
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    @Override
    public boolean isAllowed(String key) {
        if (!rateLimitConfig.isEnabled()) {
            return true;
        }
        
        try {
            // Determine limit and window based on key pattern
            int limit;
            ChronoUnit timeUnit;
            
            if (key.startsWith("ip:") && key.endsWith(":submission")) {
                limit = rateLimitConfig.getSubmissionsPerHourPerIp();
                timeUnit = ChronoUnit.HOURS;
            } else if (key.startsWith("user:") && key.endsWith(":submission")) {
                limit = rateLimitConfig.getSubmissionsPerDayPerUser();
                timeUnit = ChronoUnit.DAYS;
            } else {
                // Default to IP limits for unknown patterns
                limit = rateLimitConfig.getSubmissionsPerHourPerIp();
                timeUnit = ChronoUnit.HOURS;
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            // Clean up expired entries first
            cleanupExpiredEntries(now, timeUnit);
            
            RateLimitData data = rateLimitMap.get(key);
            
            if (data == null) {
                // First request for this key
                rateLimitMap.put(key, new RateLimitData());
                log.debug("First request allowed for key: {}", key);
                return true;
            }
            
            // Check if the window has expired
            LocalDateTime windowStart = now.minus(1, timeUnit);
            if (data.getFirstRequest().isBefore(windowStart)) {
                // Window expired, reset
                rateLimitMap.put(key, new RateLimitData());
                log.debug("Window expired, request allowed for key: {}", key);
                return true;
            }
            
            // Check if under limit
            if (data.getRequestCount() < limit) {
                data.incrementCount();
                log.debug("Request allowed for key: {} ({}/{})", key, data.getRequestCount(), limit);
                return true;
            }
            
            log.debug("Rate limit exceeded for key: {} ({}/{})", key, data.getRequestCount(), limit);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key '{}': {}", key, e.getMessage(), e);
            // In case of error, allow the request (fail open)
            return true;
        }
    }
    
    /**
     * Gets the current usage count for a given key.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @return The current number of requests in the time window
     */
    public int getCurrentUsage(String key) {
        RateLimitData data = rateLimitMap.get(key);
        return data != null ? data.getRequestCount() : 0;
    }
    
    /**
     * Clears the rate limit data for a specific key.
     * 
     * @param key The unique identifier for the rate limit bucket
     */
    public void clearRateLimit(String key) {
        rateLimitMap.remove(key);
        log.debug("Cleared rate limit data for key '{}'", key);
    }
    
    /**
     * Cleans up expired rate limit entries.
     * 
     * @param now Current time
     * @param timeUnit The time unit for expiration (hours for IP, days for users)
     */
    private void cleanupExpiredEntries(LocalDateTime now, ChronoUnit timeUnit) {
        LocalDateTime expireTime = now.minus(1, timeUnit);
        
        rateLimitMap.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getFirstRequest().isBefore(expireTime);
            if (expired) {
                log.debug("Cleaned up expired rate limit entry for key: {}", entry.getKey());
            }
            return expired;
        });
    }
    
    /**
     * Cleans up all expired rate limiting data.
     * This method should be called periodically to prevent memory leaks.
     */
    public void cleanupRateLimitData() {
        LocalDateTime now = LocalDateTime.now();
        
        // Clean up both hourly and daily windows
        cleanupExpiredEntries(now, ChronoUnit.HOURS);
        cleanupExpiredEntries(now, ChronoUnit.DAYS);
        
        log.debug("Rate limiting cleanup completed. Current map size: {}", rateLimitMap.size());
    }
}
