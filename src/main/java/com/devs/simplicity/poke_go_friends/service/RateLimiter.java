package com.devs.simplicity.poke_go_friends.service;

/**
 * Interface for rate limiting operations.
 * 
 * This interface provides a simple contract for rate limiting implementations,
 * allowing for different strategies (in-memory, Redis-based, etc.) to be used
 * interchangeably.
 */
public interface RateLimiter {
    
    /**
     * Checks if a request is allowed based on the given key.
     * 
     * @param key The unique identifier for the rate limit bucket (e.g., IP address, user ID, etc.)
     * @return true if the request is allowed, false if rate limit is exceeded
     */
    boolean isAllowed(String key);
}
