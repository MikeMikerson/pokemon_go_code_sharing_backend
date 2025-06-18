package com.devs.simplicity.poke_go_friends.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for distributed rate limiting and caching.
 * Configures RedisTemplate with String serializers and Lua scripts for atomic operations.
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Configures RedisTemplate with String serializers for both keys and values.
     * This ensures consistent serialization for rate limiting operations.
     * 
     * @param connectionFactory the Redis connection factory
     * @return configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializers for both keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        // Enable transaction support for atomic operations
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        
        log.info("Redis template configured for host: {}:{}", redisHost, redisPort);
        return template;
    }
    
    /**
     * Lua script for atomic rate limiting with SET NX and EX operations.
     * This script ensures atomic check-and-set operations for rate limiting.
     * 
     * Returns:
     * - 1 if rate limit is exceeded
     * - 0 if request is allowed
     * - TTL in seconds if key exists but not expired
     */
    @Bean("rateLimitScript")
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("""
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            
            -- Get current count
            local current = redis.call('GET', key)
            
            if current == false then
                -- First request in window
                redis.call('SET', key, 1)
                redis.call('EXPIRE', key, window)
                return 0  -- Allow request
            end
            
            current = tonumber(current)
            
            if current < limit then
                -- Under limit, increment and allow
                redis.call('INCR', key)
                return 0  -- Allow request
            else
                -- Over limit, return TTL
                local ttl = redis.call('TTL', key)
                return ttl > 0 and ttl or window  -- Return remaining TTL
            end
            """);
        
        log.debug("Rate limit Lua script configured");
        return script;
    }
    
    /**
     * Lua script for sliding window rate limiting using sorted sets.
     * This provides more precise rate limiting but uses more memory.
     * 
     * Returns:
     * - 1 if rate limit is exceeded
     * - 0 if request is allowed
     */
    @Bean("slidingWindowScript")
    public DefaultRedisScript<Long> slidingWindowScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("""
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            local identifier = ARGV[4] or current_time
            
            -- Remove expired entries
            local window_start = current_time - window * 1000
            redis.call('ZREMRANGEBYSCORE', key, 0, window_start)
            
            -- Count current entries
            local current_count = redis.call('ZCARD', key)
            
            if current_count < limit then
                -- Under limit, add current request
                redis.call('ZADD', key, current_time, identifier)
                redis.call('EXPIRE', key, window)
                return 0  -- Allow request
            else
                -- Over limit, get oldest entry time for retry calculation
                local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                if oldest[2] then
                    local retry_after = math.ceil((tonumber(oldest[2]) + window * 1000 - current_time) / 1000)
                    return retry_after > 0 and retry_after or 1
                end
                return window  -- Fallback to window size
            end
            """);
        
        log.debug("Sliding window Lua script configured");
        return script;
    }
    
    /**
     * Lua script for distributed lock-based operations.
     * Useful for ensuring only one instance can perform certain operations.
     */
    @Bean("distributedLockScript")
    public DefaultRedisScript<Long> distributedLockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("""
            local lock_key = KEYS[1]
            local identifier = ARGV[1]
            local expire = tonumber(ARGV[2])
            
            -- Try to acquire lock
            if redis.call('SET', lock_key, identifier, 'NX', 'EX', expire) then
                return 1  -- Lock acquired
            else
                return 0  -- Lock not acquired
            end
            """);
        
        log.debug("Distributed lock Lua script configured");
        return script;
    }
}
