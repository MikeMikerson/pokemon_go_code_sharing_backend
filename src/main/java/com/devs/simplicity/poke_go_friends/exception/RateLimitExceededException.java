package com.devs.simplicity.poke_go_friends.exception;

/**
 * Exception thrown when rate limiting is exceeded.
 */
public class RateLimitExceededException extends ValidationException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String identifier, String limitType) {
        super("Rate limit exceeded for " + identifier + " (" + limitType + ")");
    }
}
