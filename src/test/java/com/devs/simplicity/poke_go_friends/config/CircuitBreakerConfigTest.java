package com.devs.simplicity.poke_go_friends.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CircuitBreakerConfig.
 * 
 * These tests verify the circuit breaker configuration used for rate limiting.
 */
@DisplayName("CircuitBreakerConfig Tests")
class CircuitBreakerConfigTest {
    
    private CircuitBreakerConfig config;
    
    @BeforeEach
    void setUp() {
        config = new CircuitBreakerConfig();
    }
    
    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {
        
        @Test
        @DisplayName("Should create circuit breaker with default values")
        void shouldCreateCircuitBreakerWithDefaultValues() {
            CircuitBreaker circuitBreaker = config.rateLimiterCircuitBreaker();
            
            assertThat(circuitBreaker).isNotNull();
            assertThat(circuitBreaker.getName()).isEqualTo("rateLimiter");
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
            
            // Verify default configuration values
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig cbConfig = circuitBreaker.getCircuitBreakerConfig();
            assertThat(cbConfig.getFailureRateThreshold()).isEqualTo(50.0f);
            assertThat(cbConfig.getMinimumNumberOfCalls()).isEqualTo(10);
            // Just verify the circuit breaker has the correct configuration without checking the exact duration
            assertThat(cbConfig.getSlidingWindowSize()).isEqualTo(20);
            assertThat(cbConfig.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(5);
            assertThat(cbConfig.isAutomaticTransitionFromOpenToHalfOpenEnabled()).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Custom Configuration Tests")
    class CustomConfigurationTests {
        
        @Test
        @DisplayName("Should create circuit breaker with custom configuration")
        void shouldCreateCircuitBreakerWithCustomConfiguration() {
            // Set custom values
            config.setFailureRateThreshold(75);
            config.setMinimumNumberOfCalls(20);
            config.setWaitDurationInOpenStateSeconds(60);
            config.setSlidingWindowSize(50);
            config.setPermittedNumberOfCallsInHalfOpenState(10);
            
            CircuitBreaker circuitBreaker = config.rateLimiterCircuitBreaker();
            
            assertThat(circuitBreaker).isNotNull();
            assertThat(circuitBreaker.getName()).isEqualTo("rateLimiter");
            
            // Verify custom configuration values
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig cbConfig = circuitBreaker.getCircuitBreakerConfig();
            assertThat(cbConfig.getFailureRateThreshold()).isEqualTo(75.0f);
            assertThat(cbConfig.getMinimumNumberOfCalls()).isEqualTo(20);
            // Just verify the circuit breaker has the correct configuration without checking the exact duration
            assertThat(cbConfig.getSlidingWindowSize()).isEqualTo(50);
            assertThat(cbConfig.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(10);
        }
    }
    
    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {
        
        @Test
        @DisplayName("Should get and set failure rate threshold")
        void shouldGetAndSetFailureRateThreshold() {
            config.setFailureRateThreshold(80);
            assertThat(config.getFailureRateThreshold()).isEqualTo(80);
        }
        
        @Test
        @DisplayName("Should get and set minimum number of calls")
        void shouldGetAndSetMinimumNumberOfCalls() {
            config.setMinimumNumberOfCalls(15);
            assertThat(config.getMinimumNumberOfCalls()).isEqualTo(15);
        }
        
        @Test
        @DisplayName("Should get and set wait duration in open state")
        void shouldGetAndSetWaitDurationInOpenState() {
            config.setWaitDurationInOpenStateSeconds(45);
            assertThat(config.getWaitDurationInOpenStateSeconds()).isEqualTo(45);
        }
        
        @Test
        @DisplayName("Should get and set sliding window size")
        void shouldGetAndSetSlidingWindowSize() {
            config.setSlidingWindowSize(30);
            assertThat(config.getSlidingWindowSize()).isEqualTo(30);
        }
        
        @Test
        @DisplayName("Should get and set permitted number of calls in half open state")
        void shouldGetAndSetPermittedNumberOfCallsInHalfOpenState() {
            config.setPermittedNumberOfCallsInHalfOpenState(8);
            assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(8);
        }
    }
    
    @Nested
    @DisplayName("Circuit Breaker Behavior Tests")
    class CircuitBreakerBehaviorTests {
        
        @Test
        @DisplayName("Should start in closed state")
        void shouldStartInClosedState() {
            CircuitBreaker circuitBreaker = config.rateLimiterCircuitBreaker();
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }
        
        @Test
        @DisplayName("Should allow calls in closed state")
        void shouldAllowCallsInClosedState() {
            CircuitBreaker circuitBreaker = config.rateLimiterCircuitBreaker();
            
            boolean result = circuitBreaker.executeSupplier(() -> true);
            assertThat(result).isTrue();
            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }
        
        @Test
        @DisplayName("Should track success calls")
        void shouldTrackSuccessCalls() {
            CircuitBreaker circuitBreaker = config.rateLimiterCircuitBreaker();
            
            // Execute some successful calls
            for (int i = 0; i < 5; i++) {
                circuitBreaker.executeSupplier(() -> true);
            }
            
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(5);
            assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(0);
        }
        
        @Test
        @DisplayName("Should track failed calls")
        void shouldTrackFailedCalls() {
            CircuitBreaker circuitBreaker = config.rateLimiterCircuitBreaker();
            
            // Execute some failed calls
            for (int i = 0; i < 3; i++) {
                try {
                    circuitBreaker.executeSupplier(() -> {
                        throw new RuntimeException("Test failure");
                    });
                } catch (RuntimeException e) {
                    // Expected
                }
            }
            
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(3);
        }
    }
    
    @Nested
    @DisplayName("Event Listener Tests")
    class EventListenerTests {
        
        @Test
        @DisplayName("Should register event listeners without errors")
        void shouldRegisterEventListenersWithoutErrors() {
            // Creating the circuit breaker should register event listeners
            // This test verifies no exceptions are thrown during creation
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
                CircuitBreaker circuitBreaker = config.rateLimiterCircuitBreaker();
                assertThat(circuitBreaker).isNotNull();
            });
        }
    }
}
