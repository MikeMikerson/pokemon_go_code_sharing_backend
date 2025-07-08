# Redis Rate Limiting Implementation Plan

## Overview
The current rate limiting implementation uses an in-memory `ConcurrentHashMap` for storing rate limit data, which has significant limitations for production use. This plan outlines the migration to a Redis-based distributed rate limiting system that will provide better scalability, persistence, and multi-instance support.

## Requirements

### Functional Requirements
1.  **Distributed Rate Limiting**: Support multiple application instances sharing rate limit state.
2.  **Persistence**: Rate limit data should survive application restarts.
3.  **Performance**: Low-latency rate limit checks.
4.  **Scalability**: Handle high concurrent request volumes.
5.  **Configurable Limits**: Support different rate limit rules per endpoint type.
6.  **Fallback**: Graceful degradation when Redis is unavailable.

### Non-Functional Requirements
1.  **Reliability**: High uptime for the rate limiting service.
2.  **Consistency**: Ensure accurate rate limiting across all instances.
3.  **Maintainability**: Clean, testable code following existing patterns.

## Implementation Steps

### Step 1: Infrastructure Setup
1.  **Add Redis Dependencies**
    -   Update build.gradle to include Spring Data Redis dependencies:
        ```gradle
        implementation 'org.springframework.boot:spring-boot-starter-data-redis'
        implementation 'org.springframework.boot:spring-boot-starter-cache'
        ```

2.  **Docker Compose Configuration**
    -   Add a Redis service to compose.yaml:
        ```yaml
        redis:
          image: redis:7-alpine
          ports:
            - "6379:6379"
          volumes:
            - redis-data:/data
          command: redis-server --appendonly yes
          networks:
            - spring-postgres
        ```

3.  **Configuration Properties**
    -   Add Redis configuration to `application.properties`:
        ```properties
        # Redis Configuration
        spring.data.redis.host=localhost
        spring.data.redis.port=6379
        spring.data.redis.timeout=2000ms
        ```

### Step 2: Create Redis Rate Limiting Service
1.  **RateLimiter Interface**
    -   Create a simple interface for rate limiting operations.
        ```java
        public interface RateLimiter {
            boolean isAllowed(String key);
        }
        ```

2.  **Sliding Window Rate Limiter Implementation**
    -   Implement the `RateLimiter` interface using a sliding window algorithm with a Redis backend.
    -   Use a Lua script for atomic check-and-increment operations to prevent race conditions.

### Step 3: Replace Current Implementation
1.  **Update ValidationService**
    -   Inject the new Redis-based `RateLimiter` implementation into the `ValidationService`.
    -   Replace the existing `ConcurrentHashMap`-based logic with calls to the new service.
    -   Ensure the public API of `ValidationService` remains unchanged for backward compatibility.

2.  **Key Design Strategy**
    -   Implement a simple key structure for rate limiting:
        `rate_limit:{ipAddress}:{endpoint}`

### Step 4: Lua Script for Atomic Operations
1.  **Sliding Window Script**
    -   Use the following Lua script to atomically check and increment the rate limit in Redis.
    ```lua
    -- Atomic sliding window rate limit check and increment
    local key = KEYS[1]
    local window = tonumber(ARGV[1])
    local limit = tonumber(ARGV[2])
    local current = tonumber(ARGV[3])
    
    -- Remove expired entries
    redis.call('ZREMRANGEBYSCORE', key, 0, current - window)
    
    -- Get current count
    local count = redis.call('ZCARD', key)
    
    if count < limit then
        -- Add current timestamp
        redis.call('ZADD', key, current, current)
        redis.call('EXPIRE', key, window)
        return 1
    else
        return 0
    end
    ```

### Step 5: Circuit Breaker and Fallback
1.  **Circuit Breaker Implementation**
    -   Use Resilience4j to wrap the `RateLimiter` calls.
    -   Configure a circuit breaker to open if Redis becomes unavailable.
    -   When the circuit is open, fall back to the existing in-memory rate limiting implementation to ensure graceful degradation.

## Testing Strategy

### Unit Tests
1.  **RedisRateLimiterTest**
    -   Test the sliding window algorithm correctness.
    -   Use an embedded Redis server for fast and isolated testing of Lua scripts.
2.  **ValidationServiceTest**
    -   Update existing tests to mock the `RateLimiter` interface.
    -   Add tests for the circuit breaker and fallback behavior.

### Integration Tests
1.  **RateLimiterIntegrationTest**
    -   Test the rate limiting logic with a real Redis instance using Testcontainers.
    -   Verify the behavior under concurrent access from multiple threads.
2.  **End-to-End Tests**
    -   Test the rate limiting through HTTP endpoints to ensure it's working as expected in a full application context.