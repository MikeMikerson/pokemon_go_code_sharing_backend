package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RateLimitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InMemoryRateLimiter.
 * 
 * These tests verify the in-memory rate limiting implementation used as a fallback
 * when Redis is unavailable.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InMemoryRateLimiter Tests")
class InMemoryRateLimiterTest {
    
    @Mock
    private RateLimitConfig rateLimitConfig;
    
    private InMemoryRateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        lenient().when(rateLimitConfig.isEnabled()).thenReturn(true);
        lenient().when(rateLimitConfig.getSubmissionsPerHourPerIp()).thenReturn(5);
        lenient().when(rateLimitConfig.getSubmissionsPerDayPerUser()).thenReturn(10);
        
        rateLimiter = new InMemoryRateLimiter(rateLimitConfig);
    }
    
    @Nested
    @DisplayName("IP Rate Limiting Tests")
    class IpRateLimitingTests {
        
        @Test
        @DisplayName("Should allow requests when under IP limit")
        void shouldAllowRequestsWhenUnderIpLimit() {
            String ipKey = "ip:192.168.1.1:submission";
            
            // Should allow first 5 requests
            for (int i = 0; i < 5; i++) {
                assertThat(rateLimiter.isAllowed(ipKey))
                        .as("Request %d should be allowed", i + 1)
                        .isTrue();
            }
        }
        
        @Test
        @DisplayName("Should deny requests when IP limit exceeded")
        void shouldDenyRequestsWhenIpLimitExceeded() {
            String ipKey = "ip:192.168.1.1:submission";
            
            // Exhaust the limit
            for (int i = 0; i < 5; i++) {
                rateLimiter.isAllowed(ipKey);
            }
            
            // Next request should be denied
            assertThat(rateLimiter.isAllowed(ipKey)).isFalse();
        }
        
        @Test
        @DisplayName("Should track separate limits for different IPs")
        void shouldTrackSeparateLimitsForDifferentIps() {
            String ipKey1 = "ip:192.168.1.1:submission";
            String ipKey2 = "ip:192.168.1.2:submission";
            
            // Exhaust limit for first IP
            for (int i = 0; i < 5; i++) {
                rateLimiter.isAllowed(ipKey1);
            }
            
            // Second IP should still be allowed
            assertThat(rateLimiter.isAllowed(ipKey2)).isTrue();
        }
    }
    
    @Nested
    @DisplayName("User Rate Limiting Tests")
    class UserRateLimitingTests {
        
        @Test
        @DisplayName("Should allow requests when under user limit")
        void shouldAllowRequestsWhenUnderUserLimit() {
            String userKey = "user:123:submission";
            
            // Should allow first 10 requests
            for (int i = 0; i < 10; i++) {
                assertThat(rateLimiter.isAllowed(userKey))
                        .as("Request %d should be allowed", i + 1)
                        .isTrue();
            }
        }
        
        @Test
        @DisplayName("Should deny requests when user limit exceeded")
        void shouldDenyRequestsWhenUserLimitExceeded() {
            String userKey = "user:123:submission";
            
            // Exhaust the limit
            for (int i = 0; i < 10; i++) {
                rateLimiter.isAllowed(userKey);
            }
            
            // Next request should be denied
            assertThat(rateLimiter.isAllowed(userKey)).isFalse();
        }
        
        @Test
        @DisplayName("Should track separate limits for different users")
        void shouldTrackSeparateLimitsForDifferentUsers() {
            String userKey1 = "user:123:submission";
            String userKey2 = "user:456:submission";
            
            // Exhaust limit for first user
            for (int i = 0; i < 10; i++) {
                rateLimiter.isAllowed(userKey1);
            }
            
            // Second user should still be allowed
            assertThat(rateLimiter.isAllowed(userKey2)).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Usage Tracking Tests")
    class UsageTrackingTests {
        
        @Test
        @DisplayName("Should return current usage count")
        void shouldReturnCurrentUsageCount() {
            String key = "ip:192.168.1.1:submission";
            
            // Initial usage should be 0
            assertThat(rateLimiter.getCurrentUsage(key)).isEqualTo(0);
            
            // Make some requests
            rateLimiter.isAllowed(key);
            assertThat(rateLimiter.getCurrentUsage(key)).isEqualTo(1);
            
            rateLimiter.isAllowed(key);
            assertThat(rateLimiter.getCurrentUsage(key)).isEqualTo(2);
        }
        
        @Test
        @DisplayName("Should clear rate limit data")
        void shouldClearRateLimitData() {
            String key = "ip:192.168.1.1:submission";
            
            // Make some requests
            rateLimiter.isAllowed(key);
            rateLimiter.isAllowed(key);
            assertThat(rateLimiter.getCurrentUsage(key)).isEqualTo(2);
            
            // Clear the data
            rateLimiter.clearRateLimit(key);
            assertThat(rateLimiter.getCurrentUsage(key)).isEqualTo(0);
            
            // Should be able to make requests again
            assertThat(rateLimiter.isAllowed(key)).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should allow all requests when rate limiting disabled")
        void shouldAllowAllRequestsWhenRateLimitingDisabled() {
            when(rateLimitConfig.isEnabled()).thenReturn(false);
            
            String key = "ip:192.168.1.1:submission";
            
            // Should allow unlimited requests
            for (int i = 0; i < 100; i++) {
                assertThat(rateLimiter.isAllowed(key))
                        .as("Request %d should be allowed when rate limiting disabled", i + 1)
                        .isTrue();
            }
        }
        
        @Test
        @DisplayName("Should handle unknown key patterns with default limits")
        void shouldHandleUnknownKeyPatternsWithDefaultLimits() {
            String unknownKey = "unknown:pattern:test";
            
            // Should default to IP limits (5 requests)
            for (int i = 0; i < 5; i++) {
                assertThat(rateLimiter.isAllowed(unknownKey))
                        .as("Request %d should be allowed for unknown pattern", i + 1)
                        .isTrue();
            }
            
            // 6th request should be denied
            assertThat(rateLimiter.isAllowed(unknownKey)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Cleanup Tests")
    class CleanupTests {
        
        @Test
        @DisplayName("Should cleanup rate limit data without throwing exceptions")
        void shouldCleanupRateLimitDataWithoutThrowingExceptions() {
            String key = "ip:192.168.1.1:submission";
            
            // Make some requests
            rateLimiter.isAllowed(key);
            rateLimiter.isAllowed(key);
            
            // Cleanup should not throw exceptions
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> 
                rateLimiter.cleanupRateLimitData()
            );
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle null key gracefully")
        void shouldHandleNullKeyGracefully() {
            // Should not throw exception and should allow request (fail open)
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
                boolean result = rateLimiter.isAllowed(null);
                // Implementation should fail open for null keys
                assertThat(result).isTrue();
            });
        }
        
        @Test
        @DisplayName("Should handle empty key gracefully")
        void shouldHandleEmptyKeyGracefully() {
            // Should not throw exception and should allow request (fail open)
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
                boolean result = rateLimiter.isAllowed("");
                // Implementation should fail open for empty keys
                assertThat(result).isTrue();
            });
        }
    }
}
