package com.devs.simplicity.poke_go_friends.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for rate limiting.
 */
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
@Data
public class RateLimitConfig {
    
    /**
     * Maximum number of submissions per hour per IP address.
     */
    private int submissionsPerHourPerIp = 5;
    
    /**
     * Maximum number of submissions per day per user.
     */
    private int submissionsPerDayPerUser = 10;
    
    /**
     * Maximum number of update requests per hour per IP address.
     */
    private int updatesPerHourPerIp = 10;
    
    /**
     * Maximum number of search requests per minute per IP address.
     */
    private int searchesPerMinutePerIp = 30;
    
    /**
     * Enable or disable rate limiting globally.
     */
    private boolean enabled = true;
    
    /**
     * Rate limit cleanup interval in minutes.
     */
    private int cleanupIntervalMinutes = 60;
}
