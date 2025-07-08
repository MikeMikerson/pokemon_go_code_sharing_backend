package com.devs.simplicity.poke_go_friends.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Redis-based rate limiting.
 */
@Configuration
@ConfigurationProperties(prefix = "app.redis.rate-limit")
@Data
public class RedisRateLimitConfig {
    
    /**
     * Default window size in milliseconds (1 hour = 3600000ms).
     */
    private long defaultWindowSizeMs = 3600000L; // 1 hour
    
    /**
     * Default request limit per window.
     */
    private int defaultLimit = 5;
    
    /**
     * Key prefix for rate limiting entries in Redis.
     */
    private String keyPrefix = "rate_limit";
    
    /**
     * TTL for rate limiting keys in seconds.
     * Should be slightly larger than the window size to handle clock skew.
     */
    private long keyTtlSeconds = 3700L; // 1 hour + 5 minutes buffer
}
