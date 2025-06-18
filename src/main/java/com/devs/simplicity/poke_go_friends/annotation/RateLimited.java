package com.devs.simplicity.poke_go_friends.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation to mark methods as rate limited.
 * When applied, the annotated method will be subject to rate limiting
 * based on user fingerprinting.
 * 
 * Example usage:
 * <pre>
 * {@code
 * @RateLimited(
 *     windowSize = 24,
 *     timeUnit = TimeUnit.HOURS,
 *     maxAttempts = 1,
 *     keyPrefix = "friend_code_submission"
 * )
 * public ResponseEntity<?> submitFriendCode(HttpServletRequest request) {
 *     // method implementation
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * The size of the rate limiting window.
     * Default is 24 (when combined with TimeUnit.HOURS, this creates a 24-hour window).
     */
    int windowSize() default 24;
    
    /**
     * The time unit for the rate limiting window.
     * Default is HOURS.
     */
    TimeUnit timeUnit() default TimeUnit.HOURS;
    
    /**
     * Maximum number of attempts allowed within the time window.
     * Default is 1.
     */
    int maxAttempts() default 1;
    
    /**
     * Key prefix for Redis storage.
     * This helps organize different types of rate limits.
     * Default is "rate_limit".
     */
    String keyPrefix() default "rate_limit";
    
    /**
     * Whether to include request headers in the rate limiting key.
     * This can help differentiate between different types of clients.
     * Default is false.
     */
    boolean includeHeaders() default false;
    
    /**
     * Specific headers to include if includeHeaders is true.
     * If empty, User-Agent will be used by default.
     */
    String[] headerNames() default {};
    
    /**
     * Whether to use sliding window algorithm instead of fixed window.
     * Sliding window provides more precise rate limiting but uses more memory.
     * Default is false (uses fixed window).
     */
    boolean slidingWindow() default false;
    
    /**
     * Custom error message to return when rate limit is exceeded.
     * If empty, a default message will be used.
     */
    String errorMessage() default "";
}
