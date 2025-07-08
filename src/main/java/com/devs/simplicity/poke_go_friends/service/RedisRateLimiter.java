package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RedisRateLimitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Redis-based sliding window rate limiter implementation.
 * 
 * This implementation uses a Redis sorted set to maintain a sliding window
 * of timestamps for rate limiting. It employs a Lua script to ensure
 * atomic operations and prevent race conditions.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisRateLimitConfig config;
    
    /**
     * Lua script for atomic sliding window rate limiting.
     * 
     * This script:
     * 1. Removes expired entries from the sorted set
     * 2. Checks the current count
     * 3. If under limit, adds the current timestamp
     * 4. Sets expiration on the key
     * 5. Returns 1 if allowed, 0 if denied
     */
    private static final String LUA_SCRIPT = """
            -- Atomic sliding window rate limit check and increment
            local key = KEYS[1]
            local window = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            local current = tonumber(ARGV[3])
            local ttl = tonumber(ARGV[4])
            
            -- Remove expired entries
            redis.call('ZREMRANGEBYSCORE', key, 0, current - window)
            
            -- Get current count
            local count = redis.call('ZCARD', key)
            
            if count < limit then
                -- Add current timestamp
                redis.call('ZADD', key, current, current)
                redis.call('EXPIRE', key, ttl)
                return 1
            else
                return 0
            end
            """;
    
    private final DefaultRedisScript<Long> rateLimitScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
    
    /**
     * Checks if a request is allowed using the default configuration.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    @Override
    public boolean isAllowed(String key) {
        return isAllowed(key, config.getDefaultLimit(), config.getDefaultWindowSizeMs());
    }
    
    /**
     * Checks if a request is allowed with custom limit and window size.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @param limit The maximum number of requests allowed in the window
     * @param windowSizeMs The sliding window size in milliseconds
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    public boolean isAllowed(String key, int limit, long windowSizeMs) {
        try {
            String redisKey = buildRedisKey(key);
            long currentTimeMs = System.currentTimeMillis();
            long ttlSeconds = Math.max(config.getKeyTtlSeconds(), windowSizeMs / 1000 + 60);
            
            List<String> keys = Collections.singletonList(redisKey);
            Object[] args = {windowSizeMs, limit, currentTimeMs, ttlSeconds};
            
            Long result = redisTemplate.execute(rateLimitScript, keys, args);
            
            boolean allowed = result != null && result == 1L;
            
            if (log.isDebugEnabled()) {
                log.debug("Rate limit check for key '{}': limit={}, window={}ms, allowed={}", 
                         key, limit, windowSizeMs, allowed);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("Error executing rate limit check for key '{}': {}", key, e.getMessage(), e);
            // In case of Redis failure, allow the request (fail open)
            return true;
        }
    }
    
    /**
     * Gets the current usage count for a given key within the window.
     * 
     * @param key The unique identifier for the rate limit bucket
     * @return The current number of requests in the sliding window
     */
    public long getCurrentUsage(String key) {
        return getCurrentUsage(key, config.getDefaultWindowSizeMs());
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
            String redisKey = buildRedisKey(key);
            long currentTimeMs = System.currentTimeMillis();
            long windowStart = currentTimeMs - windowSizeMs;
            
            // Remove expired entries and count remaining
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            Long count = redisTemplate.opsForZSet().zCard(redisKey);
            
            return count != null ? count : 0L;
            
        } catch (Exception e) {
            log.error("Error getting current usage for key '{}': {}", key, e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * Clears the rate limit data for a specific key.
     * 
     * @param key The unique identifier for the rate limit bucket
     */
    public void clearRateLimit(String key) {
        try {
            String redisKey = buildRedisKey(key);
            redisTemplate.delete(redisKey);
            log.debug("Cleared rate limit data for key '{}'", key);
        } catch (Exception e) {
            log.error("Error clearing rate limit for key '{}': {}", key, e.getMessage(), e);
        }
    }
    
    /**
     * Builds the Redis key with the configured prefix.
     * 
     * @param key The original key
     * @return The Redis key with prefix
     */
    private String buildRedisKey(String key) {
        return config.getKeyPrefix() + ":" + key;
    }
}
