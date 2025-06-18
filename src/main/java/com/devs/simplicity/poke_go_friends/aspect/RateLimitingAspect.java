package com.devs.simplicity.poke_go_friends.aspect;

import com.devs.simplicity.poke_go_friends.annotation.RateLimited;
import com.devs.simplicity.poke_go_friends.dto.ErrorResponse;
import com.devs.simplicity.poke_go_friends.exception.RateLimitExceededException;
import com.devs.simplicity.poke_go_friends.service.FingerprintService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Aspect for handling rate limiting functionality.
 * Intercepts methods annotated with @RateLimited and applies rate limiting logic.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitingAspect {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final FingerprintService fingerprintService;
    
    /**
     * Around advice for rate limited methods.
     * Checks rate limits before method execution and records successful calls after.
     */
    @Around("@annotation(rateLimited)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.warn("No HTTP request found in context for rate limiting");
            return joinPoint.proceed();
        }
        
        String rateLimitKey = generateRateLimitKey(request, rateLimited);
        log.debug("Checking rate limit with key: {}", rateLimitKey);
        
        // Check if rate limit is exceeded
        if (isRateLimitExceeded(rateLimitKey, rateLimited)) {
            return handleRateLimitExceeded(rateLimitKey, rateLimited);
        }
        
        // Proceed with method execution
        Object result = joinPoint.proceed();
        
        // Record successful execution for rate limiting
        recordSuccessfulExecution(rateLimitKey, rateLimited);
        
        return result;
    }
    
    /**
     * Generates a unique rate limiting key based on user fingerprint and annotation configuration.
     */
    private String generateRateLimitKey(HttpServletRequest request, RateLimited rateLimited) {
        StringBuilder keyBuilder = new StringBuilder(rateLimited.keyPrefix()).append(":");
        
        // Add user fingerprint
        String fingerprint = fingerprintService.generateFingerprint(request);
        keyBuilder.append(fingerprint);
        
        // Add headers if configured
        if (rateLimited.includeHeaders()) {
            String[] headerNames = rateLimited.headerNames();
            if (headerNames.length == 0) {
                // Default to User-Agent if no specific headers specified
                headerNames = new String[]{"User-Agent"};
            }
            
            for (String headerName : headerNames) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null) {
                    keyBuilder.append(":").append(headerName).append(":").append(headerValue.hashCode());
                }
            }
        }
        
        // Add window identifier for fixed window (if not sliding window)
        if (!rateLimited.slidingWindow()) {
            long windowStart = getFixedWindowStart(rateLimited);
            keyBuilder.append(":").append(windowStart);
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * Checks if the rate limit has been exceeded for the given key.
     */
    private boolean isRateLimitExceeded(String key, RateLimited rateLimited) {
        if (rateLimited.slidingWindow()) {
            return isRateLimitExceededSlidingWindow(key, rateLimited);
        } else {
            return isRateLimitExceededFixedWindow(key, rateLimited);
        }
    }
    
    /**
     * Checks rate limit using fixed window algorithm.
     */
    private boolean isRateLimitExceededFixedWindow(String key, RateLimited rateLimited) {
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return false; // No previous attempts
        }
        
        try {
            int currentCount = Integer.parseInt(countStr);
            return currentCount >= rateLimited.maxAttempts();
        } catch (NumberFormatException e) {
            log.warn("Invalid count found for rate limit key {}: {}", key, countStr);
            return false; // Allow if count is corrupted
        }
    }
    
    /**
     * Checks rate limit using sliding window algorithm.
     * Uses Redis sorted sets to track timestamps of requests.
     */
    private boolean isRateLimitExceededSlidingWindow(String key, RateLimited rateLimited) {
        long windowSizeMillis = rateLimited.timeUnit().toMillis(rateLimited.windowSize());
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - windowSizeMillis;
        
        // Remove old entries outside the window
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        
        // Count current entries in the window
        Long count = redisTemplate.opsForZSet().count(key, windowStart, currentTime);
        
        return count != null && count >= rateLimited.maxAttempts();
    }
    
    /**
     * Records a successful method execution for rate limiting purposes.
     */
    private void recordSuccessfulExecution(String key, RateLimited rateLimited) {
        if (rateLimited.slidingWindow()) {
            recordSlidingWindowExecution(key, rateLimited);
        } else {
            recordFixedWindowExecution(key, rateLimited);
        }
    }
    
    /**
     * Records execution for fixed window algorithm.
     */
    private void recordFixedWindowExecution(String key, RateLimited rateLimited) {
        long ttlSeconds = rateLimited.timeUnit().toSeconds(rateLimited.windowSize());
        
        // Increment counter with TTL
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        
        log.debug("Recorded fixed window execution for key: {}", key);
    }
    
    /**
     * Records execution for sliding window algorithm.
     */
    private void recordSlidingWindowExecution(String key, RateLimited rateLimited) {
        long currentTime = System.currentTimeMillis();
        long ttlSeconds = rateLimited.timeUnit().toSeconds(rateLimited.windowSize());
        
        // Add current timestamp to sorted set
        redisTemplate.opsForZSet().add(key, String.valueOf(currentTime), currentTime);
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        
        log.debug("Recorded sliding window execution for key: {} at time: {}", key, currentTime);
    }
    
    /**
     * Handles when rate limit is exceeded by returning appropriate error response.
     */
    private Object handleRateLimitExceeded(String key, RateLimited rateLimited) {
        log.warn("Rate limit exceeded for key: {}", key);
        
        long retryAfterSeconds = calculateRetryAfter(key, rateLimited);
        Instant nextAllowedTime = Instant.now().plusSeconds(retryAfterSeconds);
        
        String errorMessage = rateLimited.errorMessage().isEmpty() 
            ? "Rate limit exceeded. Too many requests."
            : rateLimited.errorMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Rate limit exceeded")
                .message(errorMessage + " You can try again at " + nextAllowedTime)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .header("X-RateLimit-Limit", String.valueOf(rateLimited.maxAttempts()))
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(nextAllowedTime.getEpochSecond()))
                .body(errorResponse);
    }
    
    /**
     * Calculates when the user can make the next request.
     */
    private long calculateRetryAfter(String key, RateLimited rateLimited) {
        if (rateLimited.slidingWindow()) {
            return calculateRetryAfterSlidingWindow(key, rateLimited);
        } else {
            return calculateRetryAfterFixedWindow(rateLimited);
        }
    }
    
    /**
     * Calculates retry after for fixed window.
     */
    private long calculateRetryAfterFixedWindow(RateLimited rateLimited) {
        long windowSizeSeconds = rateLimited.timeUnit().toSeconds(rateLimited.windowSize());
        long currentWindowStart = getFixedWindowStart(rateLimited);
        long nextWindowStart = currentWindowStart + windowSizeSeconds;
        
        return Math.max(0, nextWindowStart - (System.currentTimeMillis() / 1000));
    }
    
    /**
     * Calculates retry after for sliding window.
     */
    private long calculateRetryAfterSlidingWindow(String key, RateLimited rateLimited) {
        long windowSizeMillis = rateLimited.timeUnit().toMillis(rateLimited.windowSize());
        
        // Get the oldest entry in the current window
        Set<String> oldestEntries = redisTemplate.opsForZSet().range(key, 0, 0);
        if (oldestEntries != null && !oldestEntries.isEmpty()) {
            try {
                long oldestTimestamp = Long.parseLong(oldestEntries.iterator().next());
                long retryAfterMillis = (oldestTimestamp + windowSizeMillis) - System.currentTimeMillis();
                return Math.max(0, retryAfterMillis / 1000);
            } catch (NumberFormatException e) {
                log.warn("Invalid timestamp in sliding window for key: {}", key);
            }
        }
        
        // Fallback to window size if we can't determine oldest entry
        return rateLimited.timeUnit().toSeconds(rateLimited.windowSize());
    }
    
    /**
     * Gets the start timestamp of the current fixed window.
     */
    private long getFixedWindowStart(RateLimited rateLimited) {
        long windowSizeSeconds = rateLimited.timeUnit().toSeconds(rateLimited.windowSize());
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        return (currentTimeSeconds / windowSizeSeconds) * windowSizeSeconds;
    }
    
    /**
     * Gets the current HTTP request from the request context.
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
