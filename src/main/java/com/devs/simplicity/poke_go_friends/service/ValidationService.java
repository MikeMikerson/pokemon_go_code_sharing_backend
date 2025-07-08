package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RateLimitConfig;
import com.devs.simplicity.poke_go_friends.exception.ValidationException;
import com.devs.simplicity.poke_go_friends.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service responsible for validating friend codes, trainer names, descriptions,
 * and implementing rate limiting and content moderation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationService {

    private final RateLimitConfig rateLimitConfig;
    private final InputSanitizationService sanitizationService;
    private final RateLimiter rateLimiter;

    // Friend code validation pattern (exactly 12 digits)
    private static final Pattern FRIEND_CODE_PATTERN = Pattern.compile("^\\d{12}$");
    
    // Enhanced trainer name validation pattern (letters and numbers only)
    private static final Pattern TRAINER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    
    // Pattern to detect suspicious character sequences
    private static final Pattern SUSPICIOUS_PATTERN = Pattern.compile("(.)\\1{4,}"); // 5+ repeated chars
    
    // Enhanced inappropriate content detection
    private static final Set<String> INAPPROPRIATE_WORDS = new HashSet<>(Arrays.asList(
        // Basic inappropriate words for demonstration
        "spam", "hack", "cheat", "bot", "fake", "scam", "sell", "buy", "money",
        "trading", "trade", "discord", "telegram", "whatsapp", "cash", "venmo",
        "paypal", "bitcoin", "crypto", "onlyfans", "adult", "xxx", "porn"
        // In production, use a more comprehensive list or external service
    ));

    /**
     * Validates a Pokemon Go friend code format.
     *
     * @param friendCode The friend code to validate
     * @throws ValidationException if the friend code is invalid
     */
    public void validateFriendCodeFormat(String friendCode) {
        log.debug("Validating friend code format: {}", friendCode);
        
        if (!StringUtils.hasText(friendCode)) {
            throw new ValidationException("Friend code cannot be empty");
        }

        // Remove any spaces or dashes that users might add
        String cleanCode = friendCode.replaceAll("[\\s-]", "");
        
        if (!FRIEND_CODE_PATTERN.matcher(cleanCode).matches()) {
            throw new ValidationException("Friend code must be exactly 12 digits");
        }

        log.debug("Friend code format validation passed: {}", friendCode);
    }

    /**
     * Validates trainer name format and content.
     *
     * @param trainerName The trainer name to validate
     * @throws ValidationException if the trainer name is invalid
     */
    public void validateTrainerName(String trainerName) {
        log.debug("Validating trainer name: {}", trainerName);
        if (!StringUtils.hasText(trainerName)) {
            // Optional field: accept null or empty
            return;
        }

        // Sanitize the trainer name
        String sanitized = sanitizationService.sanitizeTrainerName(trainerName);

        // Check if sanitization removed too much content
        if (!sanitizationService.isValidAfterSanitization(trainerName, sanitized)) {
            throw new ValidationException("Trainer name contains too many invalid characters");
        }

        if (sanitized.length() > 20) {
            throw new ValidationException("Trainer name cannot exceed 20 characters");
        }

        if (!TRAINER_NAME_PATTERN.matcher(sanitized).matches()) {
            throw new ValidationException("Trainer name can only contain letters and numbers");
        }

        // Check for suspicious patterns (repeated characters)
        if (SUSPICIOUS_PATTERN.matcher(sanitized).find()) {
            throw new ValidationException("Trainer name contains suspicious character patterns");
        }

        if (containsInappropriateContent(sanitized)) {
            throw new ValidationException("Trainer name contains inappropriate content");
        }

        log.debug("Trainer name validation passed: {}", sanitized);
    }

    /**
     * Validates player level if provided.
     *
     * @param playerLevel The player level to validate (can be null)
     * @throws ValidationException if the player level is invalid
     */
    public void validatePlayerLevel(Integer playerLevel) {
        if (playerLevel == null) {
            return; // Optional field
        }

        log.debug("Validating player level: {}", playerLevel);

        if (playerLevel < 1 || playerLevel > 50) {
            throw new ValidationException("Player level must be between 1 and 50");
        }

        log.debug("Player level validation passed: {}", playerLevel);
    }

    /**
     * Validates location format and content.
     *
     * @param location The location to validate (can be null)
     * @throws ValidationException if the location is invalid
     */
    public void validateLocation(String location) {
        if (!StringUtils.hasText(location)) {
            return; // Optional field
        }

        log.debug("Validating location: {}", location);

        // Sanitize the location
        String sanitized = sanitizationService.sanitizeLocation(location);
        
        // Check if sanitization removed too much content
        if (!sanitizationService.isValidAfterSanitization(location, sanitized)) {
            throw new ValidationException("Location contains too many invalid characters");
        }

        if (sanitized.length() > 200) {
            throw new ValidationException("Location cannot exceed 200 characters");
        }

        if (containsInappropriateContent(sanitized)) {
            throw new ValidationException("Location contains inappropriate content");
        }

        log.debug("Location validation passed: {}", sanitized);
    }

    /**
     * Validates description content.
     *
     * @param description The description to validate (can be null)
     * @throws ValidationException if the description is invalid
     */
    public void validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return; // Optional field
        }

        log.debug("Validating description: {}", description);

        // Sanitize the description
        String sanitized = sanitizationService.sanitizeDescription(description);
        
        // Check if sanitization removed too much content
        if (!sanitizationService.isValidAfterSanitization(description, sanitized)) {
            throw new ValidationException("Description contains too many invalid characters");
        }

        if (sanitized.length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }

        if (containsInappropriateContent(sanitized)) {
            throw new ValidationException("Description contains inappropriate content");
        }

        log.debug("Description validation passed");
    }

    /**
     * Checks rate limiting for IP address.
     *
     * @param ipAddress The IP address to check
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkRateLimitByIp(String ipAddress) {
        if (!rateLimitConfig.isEnabled()) {
            return;
        }
        
        log.debug("Checking rate limit for IP: {}", ipAddress);
        
        String key = "ip:" + ipAddress + ":submission";
        
        if (!rateLimiter.isAllowed(key)) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            throw new RateLimitExceededException(ipAddress, "IP hourly limit");
        }
        
        log.debug("Rate limit check passed for IP: {}", ipAddress);
    }

    /**
     * Checks rate limiting for user.
     *
     * @param userId The user ID to check
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkRateLimitByUser(Long userId) {
        if (userId == null || !rateLimitConfig.isEnabled()) {
            return; // Anonymous submissions only limited by IP
        }

        log.debug("Checking rate limit for user: {}", userId);
        
        String key = "user:" + userId + ":submission";
        
        // For user rate limiting, we need to use a 24-hour window (86400000ms)
        // Check if the rate limiter supports custom windows
        if (rateLimiter instanceof CircuitBreakerRateLimiter circuitBreakerRateLimiter) {
            long dayInMs = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
            boolean allowed = circuitBreakerRateLimiter.isAllowed(key, rateLimitConfig.getSubmissionsPerDayPerUser(), dayInMs);
            
            if (!allowed) {
                log.warn("Rate limit exceeded for user: {}", userId);
                throw new RateLimitExceededException("user:" + userId, "User daily limit");
            }
        } else if (rateLimiter instanceof RedisRateLimiter redisRateLimiter) {
            long dayInMs = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
            boolean allowed = redisRateLimiter.isAllowed(key, rateLimitConfig.getSubmissionsPerDayPerUser(), dayInMs);
            
            if (!allowed) {
                log.warn("Rate limit exceeded for user: {}", userId);
                throw new RateLimitExceededException("user:" + userId, "User daily limit");
            }
        } else {
            // Fallback for other RateLimiter implementations
            if (!rateLimiter.isAllowed(key)) {
                log.warn("Rate limit exceeded for user: {}", userId);
                throw new RateLimitExceededException("user:" + userId, "User daily limit");
            }
        }
        
        log.debug("Rate limit check passed for user: {}", userId);
    }

    /**
     * Validates all friend code submission data.
     *
     * @param friendCode   The friend code
     * @param trainerName  The trainer name
     * @param playerLevel  The player level (optional)
     * @param location     The location (optional)
     * @param description  The description (optional)
     * @param ipAddress    The submitter's IP address
     * @param userId       The submitter's user ID (optional for anonymous)
     * @throws ValidationException if any validation fails
     * @throws RateLimitExceededException if rate limits are exceeded
     */
    public void validateFriendCodeSubmission(String friendCode, String trainerName, 
                                           Integer playerLevel, String location, 
                                           String description, String ipAddress, Long userId) {
        log.info("Validating friend code submission for IP: {}, User: {}", ipAddress, userId);
        
        // Rate limiting checks first
        checkRateLimitByIp(ipAddress);
        checkRateLimitByUser(userId);
        
        // Content validation
        validateFriendCodeFormat(friendCode);
        validateTrainerName(trainerName);
        validatePlayerLevel(playerLevel);
        validateLocation(location);
        validateDescription(description);
        
        log.info("Friend code submission validation completed successfully");
    }

    /**
     * Checks if text contains inappropriate content.
     *
     * @param text The text to check
     * @return true if inappropriate content is detected
     */
    private boolean containsInappropriateContent(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        return INAPPROPRIATE_WORDS.stream()
                .anyMatch(lowerText::contains);
    }

    /**
     * Cleans up old rate limiting data (should be called periodically).
     * 
     * Note: With Redis-based rate limiting, cleanup is handled automatically
     * by the Redis expiration mechanism, so this method is effectively a no-op.
     */
    public void cleanupRateLimitData() {
        log.debug("Rate limiting cleanup - Redis handles automatic expiration");
    }

    /**
     * Gets current rate limit status for IP.
     *
     * @param ipAddress The IP address
     * @return current usage count within the hour
     */
    public int getCurrentRateLimitUsage(String ipAddress) {
        String key = "ip:" + ipAddress + ":submission";
        
        if (rateLimiter instanceof CircuitBreakerRateLimiter circuitBreakerRateLimiter) {
            return (int) circuitBreakerRateLimiter.getCurrentUsage(key);
        } else if (rateLimiter instanceof RedisRateLimiter redisRateLimiter) {
            return (int) redisRateLimiter.getCurrentUsage(key);
        } else if (rateLimiter instanceof InMemoryRateLimiter inMemoryRateLimiter) {
            return inMemoryRateLimiter.getCurrentUsage(key);
        }
        
        // Fallback for other implementations - return 0 as we can't determine usage
        return 0;
    }

}
