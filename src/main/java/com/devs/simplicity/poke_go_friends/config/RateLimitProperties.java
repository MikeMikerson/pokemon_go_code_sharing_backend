package com.devs.simplicity.poke_go_friends.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for rate limiting functionality.
 * These properties can be configured in application.properties files.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    
    /**
     * Default submission window in hours.
     */
    private int submissionWindowHours = 24;
    
    /**
     * Maximum submissions allowed per window.
     */
    private int maxSubmissionsPerWindow = 1;
    
    /**
     * Sliding window configuration.
     */
    private SlidingWindow slidingWindow = new SlidingWindow();
    
    /**
     * Distributed lock configuration.
     */
    private Lock lock = new Lock();
    
    @Data
    public static class SlidingWindow {
        /**
         * Whether to enable sliding window algorithm.
         */
        private boolean enabled = false;
        
        /**
         * Cleanup interval for sliding window entries in minutes.
         */
        private int cleanupIntervalMinutes = 60;
    }
    
    @Data
    public static class Lock {
        /**
         * Timeout for distributed locks in seconds.
         */
        private int timeoutSeconds = 30;
        
        /**
         * Retry attempts for acquiring locks.
         */
        private int retryAttempts = 3;
        
        /**
         * Retry delay between lock acquisition attempts in milliseconds.
         */
        private int retryDelayMs = 100;
    }
}
