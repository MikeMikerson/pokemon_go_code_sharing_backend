package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.annotation.RateLimited;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced rate limiting service that provides atomic Redis operations using Lua scripts.
 * Supports both fixed window and sliding window rate limiting algorithms.
 */
@Slf4j
@Service
public class EnhancedRateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final FingerprintService fingerprintService;
    
    private final DefaultRedisScript<Long> rateLimitScript;
    private final DefaultRedisScript<Long> slidingWindowScript;
    private final DefaultRedisScript<Long> distributedLockScript;
    
    public EnhancedRateLimitService(RedisTemplate<String, String> redisTemplate, 
                                   FingerprintService fingerprintService,
                                   @Qualifier("rateLimitScript") DefaultRedisScript<Long> rateLimitScript,
                                   @Qualifier("slidingWindowScript") DefaultRedisScript<Long> slidingWindowScript,
                                   @Qualifier("distributedLockScript") DefaultRedisScript<Long> distributedLockScript) {
        this.redisTemplate = redisTemplate;
        this.fingerprintService = fingerprintService;
        this.rateLimitScript = rateLimitScript;
        this.slidingWindowScript = slidingWindowScript;
        this.distributedLockScript = distributedLockScript;
    }
    
    /**
     * Checks if a request is allowed based on rate limiting configuration.
     * Uses atomic Lua scripts for consistent distributed rate limiting.
     * 
     * @param request the HTTP request
     * @param rateLimited the rate limiting configuration
     * @return RateLimitResult containing allow/deny decision and metadata
     */
    public RateLimitResult checkRateLimit(HttpServletRequest request, RateLimited rateLimited) {
        String key = generateRateLimitKey(request, rateLimited);
        long currentTime = System.currentTimeMillis();
        
        if (rateLimited.slidingWindow()) {
            return checkSlidingWindowRateLimit(key, rateLimited, currentTime);
        } else {
            return checkFixedWindowRateLimit(key, rateLimited, currentTime);
        }
    }
    
    /**
     * Checks rate limit using fixed window algorithm with Lua script.
     */
    private RateLimitResult checkFixedWindowRateLimit(String key, RateLimited rateLimited, long currentTime) {
        try {
            Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(rateLimited.maxAttempts()),
                String.valueOf(rateLimited.timeUnit().toSeconds(rateLimited.windowSize())),
                String.valueOf(currentTime / 1000)
            );
            
            if (result == null) {
                log.warn("Null result from rate limit script for key: {}", key);
                return RateLimitResult.allowed();
            }
            
            if (result == 0) {
                log.debug("Request allowed for key: {}", key);
                return RateLimitResult.allowed();
            } else {
                log.debug("Request denied for key: {}, retry after: {} seconds", key, result);
                Instant retryAfter = Instant.now().plusSeconds(result);
                return RateLimitResult.denied(result, retryAfter, rateLimited.maxAttempts());
            }
            
        } catch (Exception e) {
            log.error("Error executing rate limit script for key: {}", key, e);
            // Fail open - allow request if Redis is unavailable
            return RateLimitResult.allowed();
        }
    }
    
    /**
     * Checks rate limit using sliding window algorithm with Lua script.
     */
    private RateLimitResult checkSlidingWindowRateLimit(String key, RateLimited rateLimited, long currentTime) {
        try {
            String identifier = String.valueOf(currentTime) + "-" + Thread.currentThread().getId();
            
            Long result = redisTemplate.execute(
                slidingWindowScript,
                Collections.singletonList(key),
                String.valueOf(rateLimited.maxAttempts()),
                String.valueOf(rateLimited.windowSize()),
                String.valueOf(currentTime),
                identifier
            );
            
            if (result == null) {
                log.warn("Null result from sliding window script for key: {}", key);
                return RateLimitResult.allowed();
            }
            
            if (result == 0) {
                log.debug("Sliding window request allowed for key: {}", key);
                return RateLimitResult.allowed();
            } else {
                log.debug("Sliding window request denied for key: {}, retry after: {} seconds", key, result);
                Instant retryAfter = Instant.now().plusSeconds(result);
                return RateLimitResult.denied(result, retryAfter, rateLimited.maxAttempts());
            }
            
        } catch (Exception e) {
            log.error("Error executing sliding window script for key: {}", key, e);
            // Fail open - allow request if Redis is unavailable
            return RateLimitResult.allowed();
        }
    }
    
    /**
     * Generates a rate limiting key based on the request and configuration.
     */
    private String generateRateLimitKey(HttpServletRequest request, RateLimited rateLimited) {
        StringBuilder keyBuilder = new StringBuilder(rateLimited.keyPrefix()).append(":");
        
        // Add user fingerprint
        String fingerprint = fingerprintService.generateFingerprint(request);
        keyBuilder.append(fingerprint);
        
        // Add headers if configured
        if (rateLimited.includeHeaders()) {
            String[] headerNames = rateLimited.headerNames();
            if (headerNames.length == 0) {
                headerNames = new String[]{"User-Agent"};
            }
            
            for (String headerName : headerNames) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null) {
                    keyBuilder.append(":").append(headerName).append(":").append(headerValue.hashCode());
                }
            }
        }
        
        // For fixed window, add window identifier
        if (!rateLimited.slidingWindow()) {
            long windowSizeSeconds = rateLimited.timeUnit().toSeconds(rateLimited.windowSize());
            long currentWindowStart = (System.currentTimeMillis() / 1000) / windowSizeSeconds * windowSizeSeconds;
            keyBuilder.append(":").append(currentWindowStart);
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * Acquires a distributed lock for synchronized operations.
     * Useful for cleanup operations or other critical sections.
     * 
     * @param lockKey the lock key
     * @param identifier unique identifier for this lock holder
     * @param expireSeconds lock expiration time in seconds
     * @return true if lock was acquired, false otherwise
     */
    public boolean acquireDistributedLock(String lockKey, String identifier, int expireSeconds) {
        try {
            Long result = redisTemplate.execute(
                distributedLockScript,
                Collections.singletonList(lockKey),
                identifier,
                String.valueOf(expireSeconds)
            );
            
            boolean acquired = result != null && result == 1;
            log.debug("Distributed lock {} for key: {}", acquired ? "acquired" : "not acquired", lockKey);
            return acquired;
            
        } catch (Exception e) {
            log.error("Error acquiring distributed lock for key: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * Releases a distributed lock.
     * 
     * @param lockKey the lock key
     * @param identifier the identifier used when acquiring the lock
     * @return true if lock was released, false otherwise
     */
    public boolean releaseDistributedLock(String lockKey, String identifier) {
        try {
            // Lua script for safe lock release (only release if we own it)
            String script = """
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    return redis.call('DEL', KEYS[1])
                else
                    return 0
                end
                """;
            
            DefaultRedisScript<Long> releaseLockScript = new DefaultRedisScript<>(script, Long.class);
            Long result = redisTemplate.execute(
                releaseLockScript,
                Collections.singletonList(lockKey),
                identifier
            );
            
            boolean released = result != null && result == 1;
            log.debug("Distributed lock {} for key: {}", released ? "released" : "not released", lockKey);
            return released;
            
        } catch (Exception e) {
            log.error("Error releasing distributed lock for key: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * Result of a rate limit check.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long retryAfterSeconds;
        private final Instant retryAfterTime;
        private final int maxAttempts;
        private final int remainingAttempts;
        
        private RateLimitResult(boolean allowed, long retryAfterSeconds, Instant retryAfterTime, 
                               int maxAttempts, int remainingAttempts) {
            this.allowed = allowed;
            this.retryAfterSeconds = retryAfterSeconds;
            this.retryAfterTime = retryAfterTime;
            this.maxAttempts = maxAttempts;
            this.remainingAttempts = remainingAttempts;
        }
        
        public static RateLimitResult allowed() {
            return new RateLimitResult(true, 0, null, 0, 0);
        }
        
        public static RateLimitResult denied(long retryAfterSeconds, Instant retryAfterTime, int maxAttempts) {
            return new RateLimitResult(false, retryAfterSeconds, retryAfterTime, maxAttempts, 0);
        }
        
        // Getters
        public boolean isAllowed() { return allowed; }
        public long getRetryAfterSeconds() { return retryAfterSeconds; }
        public Instant getRetryAfterTime() { return retryAfterTime; }
        public int getMaxAttempts() { return maxAttempts; }
        public int getRemainingAttempts() { return remainingAttempts; }
    }
}
