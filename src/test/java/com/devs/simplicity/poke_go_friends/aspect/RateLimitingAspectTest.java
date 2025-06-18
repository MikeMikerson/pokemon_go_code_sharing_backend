package com.devs.simplicity.poke_go_friends.aspect;

import com.devs.simplicity.poke_go_friends.annotation.RateLimited;
import com.devs.simplicity.poke_go_friends.service.FingerprintService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingAspect")
class RateLimitingAspectTest {
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @Mock
    private FingerprintService fingerprintService;
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    private RateLimitingAspect rateLimitingAspect;
    private MockHttpServletRequest request;
    
    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimitingAspect = new RateLimitingAspect(redisTemplate, fingerprintService);
        
        request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Mozilla/5.0 Test Browser");
        
        // Set up request context for the aspect
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }
    
    @Test
    @DisplayName("should allow request when rate limit not exceeded")
    void handleRateLimit_notExceeded_shouldAllowRequest() throws Throwable {
        // Arrange
        RateLimited rateLimited = createRateLimitedAnnotation(1, 24, TimeUnit.HOURS, false);
        String expectedResponse = "Success";
        when(fingerprintService.generateFingerprint(any(HttpServletRequest.class)))
                .thenReturn("test-fingerprint");
        when(valueOperations.get(anyString())).thenReturn(null); // No previous attempts
        when(joinPoint.proceed()).thenReturn(expectedResponse);
        
        // Act
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);
        
        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }
    
    @Test
    @DisplayName("should deny request when rate limit exceeded")
    void handleRateLimit_exceeded_shouldDenyRequest() throws Throwable {
        // Arrange
        RateLimited rateLimited = createRateLimitedAnnotation(1, 24, TimeUnit.HOURS, false);
        when(fingerprintService.generateFingerprint(any(HttpServletRequest.class)))
                .thenReturn("test-fingerprint");
        when(valueOperations.get(anyString())).thenReturn("1"); // Rate limit hit
        
        // Act
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);
        
        // Assert
        assertThat(result).isInstanceOf(ResponseEntity.class);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders().getFirst("Retry-After")).isNotNull();
    }
    
    @Test
    @DisplayName("should handle missing HTTP request gracefully")
    void handleRateLimit_noRequest_shouldProceedNormally() throws Throwable {
        // Arrange
        RequestContextHolder.resetRequestAttributes(); // Remove request context
        RateLimited rateLimited = createRateLimitedAnnotation(1, 24, TimeUnit.HOURS, false);
        String expectedResponse = "Success";
        when(joinPoint.proceed()).thenReturn(expectedResponse);
        
        // Act
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);
        
        // Assert
        assertThat(result).isEqualTo(expectedResponse);
    }
    
    @Test
    @DisplayName("should use custom error message when configured")
    void handleRateLimit_customErrorMessage_shouldUseCustomMessage() throws Throwable {
        // Arrange
        String customMessage = "Custom rate limit message";
        RateLimited rateLimited = createRateLimitedAnnotationWithMessage(customMessage);
        when(fingerprintService.generateFingerprint(any(HttpServletRequest.class)))
                .thenReturn("test-fingerprint");
        when(valueOperations.get(anyString())).thenReturn("1"); // Rate limit hit
        
        // Act
        Object result = rateLimitingAspect.handleRateLimit(joinPoint, rateLimited);
        
        // Assert
        assertThat(result).isInstanceOf(ResponseEntity.class);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        // The response body should contain the custom message
        assertThat(response.getBody().toString()).contains(customMessage);
    }
    
    private RateLimited createRateLimitedAnnotation(int maxAttempts, int windowSize, TimeUnit timeUnit, boolean slidingWindow) {
        return new RateLimited() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RateLimited.class;
            }
            
            @Override
            public int windowSize() { return windowSize; }
            
            @Override
            public TimeUnit timeUnit() { return timeUnit; }
            
            @Override
            public int maxAttempts() { return maxAttempts; }
            
            @Override
            public String keyPrefix() { return "test_rate_limit"; }
            
            @Override
            public boolean includeHeaders() { return false; }
            
            @Override
            public String[] headerNames() { return new String[0]; }
            
            @Override
            public boolean slidingWindow() { return slidingWindow; }
            
            @Override
            public String errorMessage() { return ""; }
        };
    }
    
    private RateLimited createRateLimitedAnnotationWithMessage(String errorMessage) {
        return new RateLimited() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RateLimited.class;
            }
            
            @Override
            public int windowSize() { return 24; }
            
            @Override
            public TimeUnit timeUnit() { return TimeUnit.HOURS; }
            
            @Override
            public int maxAttempts() { return 1; }
            
            @Override
            public String keyPrefix() { return "test_rate_limit"; }
            
            @Override
            public boolean includeHeaders() { return false; }
            
            @Override
            public String[] headerNames() { return new String[0]; }
            
            @Override
            public boolean slidingWindow() { return false; }
            
            @Override
            public String errorMessage() { return errorMessage; }
        };
    }
}
