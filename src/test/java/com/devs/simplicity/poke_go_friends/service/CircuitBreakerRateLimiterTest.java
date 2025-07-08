package com.devs.simplicity.poke_go_friends.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CircuitBreakerRateLimiter.
 * 
 * These tests verify the circuit breaker implementation that protects Redis calls
 * and provides graceful fallback to in-memory rate limiting.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CircuitBreakerRateLimiter Tests")
class CircuitBreakerRateLimiterTest {
    
    @Mock
    private RedisRateLimiter redisRateLimiter;
    
    @Mock
    private InMemoryRateLimiter fallbackRateLimiter;
    
    @Mock
    private CircuitBreaker circuitBreaker;
    
    private CircuitBreakerRateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        rateLimiter = new CircuitBreakerRateLimiter(redisRateLimiter, fallbackRateLimiter, circuitBreaker);
    }
    
    @Nested
    @DisplayName("Normal Operation Tests")
    class NormalOperationTests {
        
        @Test
        @DisplayName("Should use Redis when circuit breaker is closed")
        void shouldUseRedisWhenCircuitBreakerIsClosed() {
            String key = "ip:192.168.1.1:submission";
            
            // Mock circuit breaker to execute the supplier normally
            when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
                java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                return supplier.get();
            });
            when(redisRateLimiter.isAllowed(key)).thenReturn(true);
            
            boolean result = rateLimiter.isAllowed(key);
            
            assertThat(result).isTrue();
            verify(redisRateLimiter, times(1)).isAllowed(key);
            verify(fallbackRateLimiter, never()).isAllowed(anyString());
        }
        
        @Test
        @DisplayName("Should use Redis for custom limit and window")
        void shouldUseRedisForCustomLimitAndWindow() {
            String key = "user:123:submission";
            int limit = 10;
            long windowMs = 86400000L;
            
            // Mock circuit breaker to execute the supplier normally
            when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
                java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                return supplier.get();
            });
            when(redisRateLimiter.isAllowed(key, limit, windowMs)).thenReturn(true);
            
            boolean result = rateLimiter.isAllowed(key, limit, windowMs);
            
            assertThat(result).isTrue();
            verify(redisRateLimiter, times(1)).isAllowed(key, limit, windowMs);
            verify(fallbackRateLimiter, never()).isAllowed(anyString());
        }
    }
    
    @Nested
    @DisplayName("Circuit Breaker Open Tests")
    class CircuitBreakerOpenTests {
        
        @Test
        @DisplayName("Should fallback to in-memory when circuit breaker throws exception")
        void shouldFallbackToInMemoryWhenCircuitBreakerThrowsException() {
            String key = "ip:192.168.1.1:submission";
            
            // Mock circuit breaker to throw any exception
            when(circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Circuit breaker is open"));
            when(fallbackRateLimiter.isAllowed(key)).thenReturn(true);
            
            boolean result = rateLimiter.isAllowed(key);
            
            assertThat(result).isTrue();
            verify(fallbackRateLimiter, times(1)).isAllowed(key);
            verify(redisRateLimiter, never()).isAllowed(anyString());
        }
        
        @Test
        @DisplayName("Should fallback to in-memory for custom limit when circuit breaker throws exception")
        void shouldFallbackToInMemoryForCustomLimitWhenCircuitBreakerThrowsException() {
            String key = "user:123:submission";
            int limit = 10;
            long windowMs = 86400000L;
            
            // Mock circuit breaker to throw any exception
            when(circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Circuit breaker is open"));
            when(fallbackRateLimiter.isAllowed(key)).thenReturn(false);
            
            boolean result = rateLimiter.isAllowed(key, limit, windowMs);
            
            assertThat(result).isFalse();
            verify(fallbackRateLimiter, times(1)).isAllowed(key);
            verify(redisRateLimiter, never()).isAllowed(anyString(), anyInt(), anyLong());
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should fallback to in-memory on unexpected Redis error")
        void shouldFallbackToInMemoryOnUnexpectedRedisError() {
            String key = "ip:192.168.1.1:submission";
            
            // Mock circuit breaker to throw unexpected exception
            when(circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Unexpected error"));
            when(fallbackRateLimiter.isAllowed(key)).thenReturn(true);
            
            boolean result = rateLimiter.isAllowed(key);
            
            assertThat(result).isTrue();
            verify(fallbackRateLimiter, times(1)).isAllowed(key);
        }
        
        @Test
        @DisplayName("Should fallback to in-memory on unexpected Redis error for custom limit")
        void shouldFallbackToInMemoryOnUnexpectedRedisErrorForCustomLimit() {
            String key = "user:123:submission";
            int limit = 10;
            long windowMs = 86400000L;
            
            // Mock circuit breaker to throw unexpected exception
            when(circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Unexpected error"));
            when(fallbackRateLimiter.isAllowed(key)).thenReturn(false);
            
            boolean result = rateLimiter.isAllowed(key, limit, windowMs);
            
            assertThat(result).isFalse();
            verify(fallbackRateLimiter, times(1)).isAllowed(key);
        }
    }
    
    @Nested
    @DisplayName("Usage Tracking Tests")
    class UsageTrackingTests {
        
        @Test
        @DisplayName("Should get usage from Redis when circuit breaker is closed")
        void shouldGetUsageFromRedisWhenCircuitBreakerIsClosed() {
            String key = "ip:192.168.1.1:submission";
            long expectedUsage = 3L;
            
            // Mock circuit breaker to execute the supplier normally
            when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
                java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                return supplier.get();
            });
            when(redisRateLimiter.getCurrentUsage(key)).thenReturn(expectedUsage);
            
            long result = rateLimiter.getCurrentUsage(key);
            
            assertThat(result).isEqualTo(expectedUsage);
            verify(redisRateLimiter, times(1)).getCurrentUsage(key);
            verify(fallbackRateLimiter, never()).getCurrentUsage(anyString());
        }
        
        @Test
        @DisplayName("Should fallback to in-memory usage when circuit breaker throws exception")
        void shouldFallbackToInMemoryUsageWhenCircuitBreakerThrowsException() {
            String key = "ip:192.168.1.1:submission";
            int expectedUsage = 2;
            
            // Mock circuit breaker to throw any exception
            when(circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Circuit breaker is open"));
            when(fallbackRateLimiter.getCurrentUsage(key)).thenReturn(expectedUsage);
            
            long result = rateLimiter.getCurrentUsage(key);
            
            assertThat(result).isEqualTo(expectedUsage);
            verify(fallbackRateLimiter, times(1)).getCurrentUsage(key);
            verify(redisRateLimiter, never()).getCurrentUsage(anyString());
        }
        
        @Test
        @DisplayName("Should get usage with custom window from Redis when circuit breaker is closed")
        void shouldGetUsageWithCustomWindowFromRedisWhenCircuitBreakerIsClosed() {
            String key = "user:123:submission";
            long windowMs = 86400000L;
            long expectedUsage = 5L;
            
            // Mock circuit breaker to execute the supplier normally
            when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
                java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                return supplier.get();
            });
            when(redisRateLimiter.getCurrentUsage(key, windowMs)).thenReturn(expectedUsage);
            
            long result = rateLimiter.getCurrentUsage(key, windowMs);
            
            assertThat(result).isEqualTo(expectedUsage);
            verify(redisRateLimiter, times(1)).getCurrentUsage(key, windowMs);
            verify(fallbackRateLimiter, never()).getCurrentUsage(anyString());
        }
    }
    
    @Nested
    @DisplayName("Clear Rate Limit Tests")
    class ClearRateLimitTests {
        
        @Test
        @DisplayName("Should clear Redis data when circuit breaker is closed")
        void shouldClearRedisDataWhenCircuitBreakerIsClosed() {
            String key = "ip:192.168.1.1:submission";
            
            // Mock circuit breaker to execute the supplier normally
            when(circuitBreaker.executeSupplier(any())).thenAnswer(invocation -> {
                java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                return supplier.get();
            });
            
            rateLimiter.clearRateLimit(key);
            
            verify(redisRateLimiter, times(1)).clearRateLimit(key);
            verify(fallbackRateLimiter, never()).clearRateLimit(anyString());
        }
        
        @Test
        @DisplayName("Should fallback to in-memory clear when circuit breaker throws exception")
        void shouldFallbackToInMemoryClearWhenCircuitBreakerThrowsException() {
            String key = "ip:192.168.1.1:submission";
            
            // Mock circuit breaker to throw any exception
            when(circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Circuit breaker is open"));
            
            rateLimiter.clearRateLimit(key);
            
            verify(fallbackRateLimiter, times(1)).clearRateLimit(key);
            verify(redisRateLimiter, never()).clearRateLimit(anyString());
        }
        
        @Test
        @DisplayName("Should fallback to in-memory clear on unexpected error")
        void shouldFallbackToInMemoryClearOnUnexpectedError() {
            String key = "ip:192.168.1.1:submission";
            
            // Mock circuit breaker to throw unexpected exception
            when(circuitBreaker.executeSupplier(any())).thenThrow(new RuntimeException("Unexpected error"));
            
            rateLimiter.clearRateLimit(key);
            
            verify(fallbackRateLimiter, times(1)).clearRateLimit(key);
        }
    }
    
    @Nested
    @DisplayName("Circuit Breaker Monitoring Tests")
    class CircuitBreakerMonitoringTests {
        
        @Test
        @DisplayName("Should expose circuit breaker state")
        void shouldExposeCircuitBreakerState() {
            CircuitBreaker.State expectedState = CircuitBreaker.State.CLOSED;
            when(circuitBreaker.getState()).thenReturn(expectedState);
            
            CircuitBreaker.State result = rateLimiter.getCircuitBreakerState();
            
            assertThat(result).isEqualTo(expectedState);
            verify(circuitBreaker, times(1)).getState();
        }
        
        @Test
        @DisplayName("Should expose circuit breaker metrics")
        void shouldExposeCircuitBreakerMetrics() {
            CircuitBreaker.Metrics mockMetrics = circuitBreaker.getMetrics();
            when(circuitBreaker.getMetrics()).thenReturn(mockMetrics);
            
            CircuitBreaker.Metrics result = rateLimiter.getCircuitBreakerMetrics();
            
            assertThat(result).isEqualTo(mockMetrics);
            verify(circuitBreaker, times(2)).getMetrics(); // Called once in setup, once in test
        }
    }
    
    @Nested
    @DisplayName("Cleanup Tests")
    class CleanupTests {
        
        @Test
        @DisplayName("Should cleanup both stores without using circuit breaker")
        void shouldCleanupBothStoresWithoutUsingCircuitBreaker() {
            rateLimiter.cleanupRateLimitData();
            
            // Should always clean up in-memory store
            verify(fallbackRateLimiter, times(1)).cleanupRateLimitData();
            
            // Should not use circuit breaker for cleanup operations
            verify(circuitBreaker, never()).executeSupplier(any());
        }
    }
}
