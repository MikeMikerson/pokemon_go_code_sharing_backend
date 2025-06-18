package com.devs.simplicity.poke_go_friends.exception;

import lombok.Getter;

import java.time.Instant;

/**
 * Exception thrown when rate limiting threshold is exceeded.
 * Contains information about when the next request can be made.
 */
@Getter
public class RateLimitExceededException extends RuntimeException {
    
    private final String userFingerprint;
    private final Instant nextAllowedTime;
    private final long retryAfterSeconds;
    private final int maxAttempts;
    private final int currentAttempts;
    
    public RateLimitExceededException(String message, String userFingerprint, 
                                   Instant nextAllowedTime, long retryAfterSeconds,
                                   int maxAttempts, int currentAttempts) {
        super(message);
        this.userFingerprint = userFingerprint;
        this.nextAllowedTime = nextAllowedTime;
        this.retryAfterSeconds = retryAfterSeconds;
        this.maxAttempts = maxAttempts;
        this.currentAttempts = currentAttempts;
    }
    
    public RateLimitExceededException(String message, String userFingerprint, 
                                   Instant nextAllowedTime, long retryAfterSeconds) {
        this(message, userFingerprint, nextAllowedTime, retryAfterSeconds, 0, 0);
    }
}
