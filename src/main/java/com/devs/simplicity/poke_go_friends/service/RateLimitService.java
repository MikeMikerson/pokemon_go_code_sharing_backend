package com.devs.simplicity.poke_go_friends.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing rate limiting using Redis.
 * Implements a 24-hour cooldown period for friend code submissions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final Duration COOLDOWN_PERIOD = Duration.ofHours(24);
    private static final long COOLDOWN_TTL_HOURS = 24L;

    private final RedisTemplate<String, String> redisTemplate;
    private final FingerprintService fingerprintService;

    /**
     * Checks if a user can submit a friend code based on their request.
     * 
     * @param request the HTTP request to generate fingerprint from
     * @return true if the user can submit, false if within cooldown period
     */
    public boolean canSubmit(HttpServletRequest request) {
        String userFingerprint = fingerprintService.generateFingerprint(request);
        return canSubmit(userFingerprint);
    }

    /**
     * Records a submission timestamp for rate limiting based on the request.
     * 
     * @param request the HTTP request to generate fingerprint from
     */
    public void recordSubmission(HttpServletRequest request) {
        String userFingerprint = fingerprintService.generateFingerprint(request);
        recordSubmission(userFingerprint);
    }

    /**
     * Calculates the next allowed submission time for a user based on their request.
     * 
     * @param request the HTTP request to generate fingerprint from
     * @return the instant when the user can next submit, or current time if they can submit now
     */
    public Instant getNextAllowedSubmissionTime(HttpServletRequest request) {
        String userFingerprint = fingerprintService.generateFingerprint(request);
        return getNextAllowedSubmissionTime(userFingerprint);
    }

    /**
     * Checks if a user can submit a friend code based on their fingerprint.
     * 
     * @param userFingerprint the user's unique fingerprint
     * @return true if the user can submit, false if within cooldown period
     */
    public boolean canSubmit(String userFingerprint) {
        log.debug("Checking rate limit for user fingerprint: {}", userFingerprint);
        
        String key = RATE_LIMIT_PREFIX + userFingerprint;
        String lastSubmissionStr = redisTemplate.opsForValue().get(key);
        
        if (lastSubmissionStr == null) {
            log.debug("No previous submission found for user fingerprint: {}", userFingerprint);
            return true;
        }
        
        try {
            long lastSubmissionTime = Long.parseLong(lastSubmissionStr);
            Instant lastSubmission = Instant.ofEpochMilli(lastSubmissionTime);
            Instant now = Instant.now();
            
            boolean canSubmit = now.isAfter(lastSubmission.plus(COOLDOWN_PERIOD));
            
            if (canSubmit) {
                log.debug("Cooldown period elapsed for user fingerprint: {}", userFingerprint);
            } else {
                log.debug("User fingerprint {} is still within cooldown period", userFingerprint);
            }
            
            return canSubmit;
        } catch (NumberFormatException e) {
            log.warn("Invalid timestamp found for user fingerprint {}: {}", userFingerprint, lastSubmissionStr);
            return true; // Allow submission if timestamp is corrupted
        }
    }

    /**
     * Records a submission timestamp for rate limiting.
     * 
     * @param userFingerprint the user's unique fingerprint
     */
    public void recordSubmission(String userFingerprint) {
        log.debug("Recording submission for user fingerprint: {}", userFingerprint);
        
        String key = RATE_LIMIT_PREFIX + userFingerprint;
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        
        redisTemplate.opsForValue().set(key, timestamp, COOLDOWN_TTL_HOURS, TimeUnit.HOURS);
        
        log.debug("Submission recorded for user fingerprint: {} with timestamp: {}", userFingerprint, timestamp);
    }

    /**
     * Calculates the next allowed submission time for a user.
     * 
     * @param userFingerprint the user's unique fingerprint
     * @return the instant when the user can next submit, or current time if they can submit now
     */
    public Instant getNextAllowedSubmissionTime(String userFingerprint) {
        log.debug("Calculating next allowed submission time for user fingerprint: {}", userFingerprint);
        
        String key = RATE_LIMIT_PREFIX + userFingerprint;
        String lastSubmissionStr = redisTemplate.opsForValue().get(key);
        
        if (lastSubmissionStr == null) {
            return Instant.now();
        }
        
        try {
            long lastSubmissionTime = Long.parseLong(lastSubmissionStr);
            Instant lastSubmission = Instant.ofEpochMilli(lastSubmissionTime);
            Instant nextAllowed = lastSubmission.plus(COOLDOWN_PERIOD);
            Instant now = Instant.now();
            
            return nextAllowed.isAfter(now) ? nextAllowed : now;
        } catch (NumberFormatException e) {
            log.warn("Invalid timestamp found for user fingerprint {}: {}", userFingerprint, lastSubmissionStr);
            return Instant.now(); // Allow immediate submission if timestamp is corrupted
        }
    }
}
