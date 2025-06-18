# Rate Limiting Implementation

This document describes the comprehensive rate limiting implementation for the Pokémon Go Friend Code Sharing API.

## Overview

The rate limiting system provides robust, distributed rate limiting using Redis with both fixed window and sliding window algorithms. It uses AOP (Aspect-Oriented Programming) to apply rate limiting declaratively via annotations.

## Features

### 1. Annotation-Based Rate Limiting
- **`@RateLimited`** annotation for declarative rate limiting
- Configurable window size, time units, and attempt limits
- Support for custom error messages
- Header-based fingerprinting options

### 2. Dual Algorithm Support
- **Fixed Window**: Traditional time-based windows with reset intervals
- **Sliding Window**: More precise rate limiting using sorted sets
- Configurable per annotation or globally

### 3. Distributed Redis Operations
- Atomic Lua scripts for consistent rate limiting across instances
- Redis SET with NX and EX for distributed locks
- Automatic cleanup of expired entries
- Connection pooling and error handling

### 4. Enhanced Error Responses
- HTTP 429 Too Many Requests with proper headers
- `Retry-After` header with seconds until next allowed request
- `X-RateLimit-*` headers for client information
- Custom error messages per endpoint

## Usage

### Basic Usage

```java
@PostMapping("/api/friend-codes")
@RateLimited(
    windowSize = 24,
    timeUnit = TimeUnit.HOURS,
    maxAttempts = 1,
    keyPrefix = "friend_code_submission"
)
public ResponseEntity<?> submitFriendCode(@Valid @RequestBody FriendCodeSubmissionRequest request) {
    // Implementation
}
```

### Advanced Configuration

```java
@RateLimited(
    windowSize = 1,
    timeUnit = TimeUnit.HOURS,
    maxAttempts = 10,
    keyPrefix = "api_calls",
    slidingWindow = true,
    includeHeaders = true,
    headerNames = {"User-Agent", "X-API-Key"},
    errorMessage = "API rate limit exceeded. Please wait before making more requests."
)
```

## Configuration

### Application Properties

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=2000ms
spring.data.redis.connect-timeout=2000ms
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
spring.data.redis.lettuce.pool.max-wait=-1ms

# Rate Limiting Configuration
app.rate-limit.submission-window-hours=24
app.rate-limit.max-submissions-per-window=1
app.rate-limit.sliding-window.enabled=false
app.rate-limit.lock.timeout-seconds=30
```

## Components

### 1. Core Components

- **`@RateLimited`**: Annotation for marking rate-limited methods
- **`RateLimitingAspect`**: AOP aspect that intercepts annotated methods
- **`EnhancedRateLimitService`**: Service providing Lua script-based operations
- **`RateLimitProperties`**: Configuration properties binding

### 2. Supporting Components

- **`RateLimitExceededException`**: Exception for rate limit violations
- **`RedisConfig`**: Enhanced Redis configuration with Lua scripts
- **`FingerprintService`**: User identification for rate limiting

### 3. Lua Scripts

#### Fixed Window Script
```lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])

local current = redis.call('GET', key)
if current == false then
    redis.call('SET', key, 1)
    redis.call('EXPIRE', key, window)
    return 0  -- Allow request
end

current = tonumber(current)
if current < limit then
    redis.call('INCR', key)
    return 0  -- Allow request
else
    local ttl = redis.call('TTL', key)
    return ttl > 0 and ttl or window  -- Return remaining TTL
end
```

#### Sliding Window Script
```lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])
local identifier = ARGV[4] or current_time

-- Remove expired entries
local window_start = current_time - window * 1000
redis.call('ZREMRANGEBYSCORE', key, 0, window_start)

-- Count current entries
local current_count = redis.call('ZCARD', key)

if current_count < limit then
    redis.call('ZADD', key, current_time, identifier)
    redis.call('EXPIRE', key, window)
    return 0  -- Allow request
else
    local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
    if oldest[2] then
        local retry_after = math.ceil((tonumber(oldest[2]) + window * 1000 - current_time) / 1000)
        return retry_after > 0 and retry_after or 1
    end
    return window
end
```

## Error Handling

### Rate Limit Exceeded Response

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 3600
X-RateLimit-Limit: 1
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1640995200
Content-Type: application/json

{
  "error": "Rate limit exceeded",
  "message": "You can only submit one friend code per 24 hours. You can try again at 2022-01-01T12:00:00Z",
  "timestamp": "2022-01-01T11:00:00Z"
}
```

### Graceful Degradation

- **Redis Unavailable**: System fails open, allowing requests
- **Script Errors**: Logged and fallback to allow
- **Invalid Data**: Cleaned up automatically

## Testing

### Unit Tests
- **`RateLimitingAspectTest`**: Tests AOP aspect behavior
- **`EnhancedRateLimitServiceTest`**: Tests service logic and Lua scripts
- **`RateLimitServiceTest`**: Tests existing rate limit service

### Integration Tests
- **`RateLimitingIntegrationTest`**: End-to-end rate limiting verification
- **`FriendCodeControllerTest`**: Controller-level testing with rate limits

## Performance Considerations

### Fixed Window vs Sliding Window

| Algorithm | Memory Usage | Precision | Performance |
|-----------|--------------|-----------|-------------|
| Fixed Window | Low | Burst-prone | High |
| Sliding Window | High | Precise | Moderate |

### Redis Memory Usage

- **Fixed Window**: O(1) per window per user
- **Sliding Window**: O(n) where n = requests in window
- **Cleanup**: Automatic TTL-based expiration

## Monitoring

### Key Metrics to Monitor

1. **Rate Limit Hit Rate**: Percentage of requests rate limited
2. **Redis Connection Health**: Pool usage and connection errors
3. **Lua Script Performance**: Execution time and error rates
4. **Memory Usage**: Redis memory consumption for rate limit data

### Logs

```java
// Debug logs for development
log.debug("Checking rate limit with key: {}", rateLimitKey);
log.debug("Request allowed for key: {}", key);

// Warning logs for rate limit hits
log.warn("Rate limit exceeded for key: {}", key);

// Error logs for Redis issues
log.error("Error executing rate limit script for key: {}", key, e);
```

## Security Considerations

1. **Fingerprint Privacy**: IP addresses are hashed for privacy
2. **Key Namespacing**: Prevents key collisions between rate limit types
3. **Script Injection**: Lua scripts are pre-compiled and parameterized
4. **Memory DoS**: TTL ensures automatic cleanup of rate limit data

## Future Enhancements

1. **Adaptive Rate Limiting**: Dynamic limits based on system load
2. **Geographic Rate Limiting**: Different limits per region
3. **User Tier Rate Limiting**: Different limits for different user types
4. **Rate Limit Analytics**: Dashboard for rate limiting metrics
5. **Circuit Breaker Integration**: Automatic degradation during high load
