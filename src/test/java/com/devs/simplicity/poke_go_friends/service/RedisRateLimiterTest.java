package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RedisRateLimitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisRateLimiter.
 * 
 * These tests focus on the business logic and behavior of the rate limiter
 * without requiring an actual Redis instance.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisRateLimiter")
class RedisRateLimiterTest {
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    
    private RedisRateLimitConfig config;
    private RedisRateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        config = new RedisRateLimitConfig();
        config.setDefaultLimit(5);
        config.setDefaultWindowSizeMs(3600000L); // 1 hour
        config.setKeyPrefix("rate_limit");
        config.setKeyTtlSeconds(3700L);
        
        rateLimiter = new RedisRateLimiter(redisTemplate, config);
    }
    
    @Nested
    @DisplayName("isAllowed")
    class IsAllowedTests {
        
        @Test
        @DisplayName("should allow request when under limit")
        void shouldAllowRequest_whenUnderLimit() {
            // Given
            String key = "test-key";
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L);
            
            // When
            boolean result = rateLimiter.isAllowed(key);
            
            // Then
            assertThat(result).isTrue();
            verify(redisTemplate).execute(any(DefaultRedisScript.class), 
                                        eq(Collections.singletonList("rate_limit:test-key")), 
                                        any(Object[].class));
        }
        
        @Test
        @DisplayName("should deny request when limit exceeded")
        void shouldDenyRequest_whenLimitExceeded() {
            // Given
            String key = "test-key";
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(0L);
            
            // When
            boolean result = rateLimiter.isAllowed(key);
            
            // Then
            assertThat(result).isFalse();
        }
        
        @Test
        @DisplayName("should allow request when Redis fails")
        void shouldAllowRequest_whenRedisFailsFailOpen() {
            // Given
            String key = "test-key";
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class)))
                .thenThrow(new RuntimeException("Redis connection failed"));
            
            // When
            boolean result = rateLimiter.isAllowed(key);
            
            // Then
            assertThat(result).isTrue();
        }
        
        @Test
        @DisplayName("should handle null result from Redis")
        void shouldHandleNullResult_fromRedis() {
            // Given
            String key = "test-key";
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(null);
            
            // When
            boolean result = rateLimiter.isAllowed(key);
            
            // Then
            assertThat(result).isFalse();
        }
        
        @Test
        @DisplayName("should use custom limit and window")
        void shouldUseCustomLimitAndWindow() {
            // Given
            String key = "test-key";
            int customLimit = 10;
            long customWindow = 60000L; // 1 minute
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L);
            
            // When
            boolean result = rateLimiter.isAllowed(key, customLimit, customWindow);
            
            // Then
            assertThat(result).isTrue();
            verify(redisTemplate).execute(any(DefaultRedisScript.class), 
                                        eq(Collections.singletonList("rate_limit:test-key")), 
                                        any(Object[].class));
        }
    }
    
    @Nested
    @DisplayName("getCurrentUsage")
    class GetCurrentUsageTests {
        
        @Test
        @DisplayName("should return current usage count")
        void shouldReturnCurrentUsageCount() {
            // Given
            String key = "test-key";
            long expectedCount = 3L;
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.zCard("rate_limit:test-key")).thenReturn(expectedCount);
            
            // When
            long result = rateLimiter.getCurrentUsage(key);
            
            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(zSetOperations).removeRangeByScore(eq("rate_limit:test-key"), eq(0.0), anyDouble());
            verify(zSetOperations).zCard("rate_limit:test-key");
        }
        
        @Test
        @DisplayName("should return zero when Redis returns null")
        void shouldReturnZero_whenRedisReturnsNull() {
            // Given
            String key = "test-key";
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.zCard("rate_limit:test-key")).thenReturn(null);
            
            // When
            long result = rateLimiter.getCurrentUsage(key);
            
            // Then
            assertThat(result).isZero();
        }
        
        @Test
        @DisplayName("should return zero when Redis fails")
        void shouldReturnZero_whenRedisFails() {
            // Given
            String key = "test-key";
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("Redis connection failed"));
            
            // When
            long result = rateLimiter.getCurrentUsage(key);
            
            // Then
            assertThat(result).isZero();
        }
        
        @Test
        @DisplayName("should use custom window size")
        void shouldUseCustomWindowSize() {
            // Given
            String key = "test-key";
            long customWindow = 60000L; // 1 minute
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.zCard("rate_limit:test-key")).thenReturn(2L);
            
            // When
            long result = rateLimiter.getCurrentUsage(key, customWindow);
            
            // Then
            assertThat(result).isEqualTo(2L);
            verify(zSetOperations).removeRangeByScore(eq("rate_limit:test-key"), 
                                                    eq(0.0), 
                                                    anyDouble());
        }
    }
    
    @Nested
    @DisplayName("clearRateLimit")
    class ClearRateLimitTests {
        
        @Test
        @DisplayName("should clear rate limit data")
        void shouldClearRateLimitData() {
            // Given
            String key = "test-key";
            
            // When
            rateLimiter.clearRateLimit(key);
            
            // Then
            verify(redisTemplate).delete("rate_limit:test-key");
        }
        
        @Test
        @DisplayName("should handle Redis failures gracefully")
        void shouldHandleRedisFailuresGracefully() {
            // Given
            String key = "test-key";
            when(redisTemplate.delete("rate_limit:test-key"))
                .thenThrow(new RuntimeException("Redis connection failed"));
            
            // When & Then (should not throw exception)
            rateLimiter.clearRateLimit(key);
            
            verify(redisTemplate).delete("rate_limit:test-key");
        }
    }
    
    @Nested
    @DisplayName("Key Building")
    class KeyBuildingTests {
        
        @Test
        @DisplayName("should build Redis key with prefix")
        void shouldBuildRedisKeyWithPrefix() {
            // Given
            String key = "user:123";
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L);
            
            // When
            rateLimiter.isAllowed(key);
            
            // Then
            verify(redisTemplate).execute(any(DefaultRedisScript.class), 
                                        eq(Collections.singletonList("rate_limit:user:123")), 
                                        any(Object[].class));
        }
    }
}
