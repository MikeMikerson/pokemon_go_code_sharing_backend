package com.devs.simplicity.poke_go_friends.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService")
class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private FingerprintService fingerprintService;

    private RateLimitService rateLimitService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimitService = new RateLimitService(redisTemplate, fingerprintService);
        
        request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
    }

    @Test
    @DisplayName("should allow submission when no previous submission exists")
    void canSubmit_noPreviousSubmission_shouldReturnTrue() {
        // Arrange
        String userFingerprint = "test-fingerprint";
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        boolean canSubmit = rateLimitService.canSubmit(userFingerprint);

        // Assert
        assertThat(canSubmit).isTrue();
        verify(valueOperations).get("rate_limit:" + userFingerprint);
    }

    @Test
    @DisplayName("should deny submission when cooldown period has not elapsed")
    void canSubmit_withinCooldownPeriod_shouldReturnFalse() {
        // Arrange
        String userFingerprint = "test-fingerprint";
        String recentTimestamp = String.valueOf(Instant.now().toEpochMilli());
        when(valueOperations.get(anyString())).thenReturn(recentTimestamp);

        // Act
        boolean canSubmit = rateLimitService.canSubmit(userFingerprint);

        // Assert
        assertThat(canSubmit).isFalse();
        verify(valueOperations).get("rate_limit:" + userFingerprint);
    }

    @Test
    @DisplayName("should allow submission when cooldown period has elapsed")
    void canSubmit_afterCooldownPeriod_shouldReturnTrue() {
        // Arrange
        String userFingerprint = "test-fingerprint";
        long oldTimestamp = Instant.now().minus(Duration.ofHours(25)).toEpochMilli();
        when(valueOperations.get(anyString())).thenReturn(String.valueOf(oldTimestamp));

        // Act
        boolean canSubmit = rateLimitService.canSubmit(userFingerprint);

        // Assert
        assertThat(canSubmit).isTrue();
        verify(valueOperations).get("rate_limit:" + userFingerprint);
    }

    @Test
    @DisplayName("should record submission timestamp in Redis with TTL")
    void recordSubmission_shouldStoreTimestampWithTtl() {
        // Arrange
        String userFingerprint = "test-fingerprint";

        // Act
        rateLimitService.recordSubmission(userFingerprint);

        // Assert
        verify(valueOperations).set(eq("rate_limit:" + userFingerprint), anyString(), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("should calculate next submission time when within cooldown")
    void getNextAllowedSubmissionTime_withinCooldown_shouldReturnFutureTime() {
        // Arrange
        String userFingerprint = "test-fingerprint";
        long recentTimestamp = Instant.now().toEpochMilli();
        when(valueOperations.get(anyString())).thenReturn(String.valueOf(recentTimestamp));

        // Act
        Instant nextAllowed = rateLimitService.getNextAllowedSubmissionTime(userFingerprint);

        // Assert
        assertThat(nextAllowed).isAfter(Instant.now());
        assertThat(nextAllowed).isBefore(Instant.now().plus(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("should return current time when no submission exists")
    void getNextAllowedSubmissionTime_noSubmission_shouldReturnCurrentTime() {
        // Arrange
        String userFingerprint = "test-fingerprint";
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        Instant nextAllowed = rateLimitService.getNextAllowedSubmissionTime(userFingerprint);

        // Assert
        assertThat(nextAllowed).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("should return current time when cooldown has elapsed")
    void getNextAllowedSubmissionTime_afterCooldown_shouldReturnCurrentTime() {
        // Arrange
        String userFingerprint = "test-fingerprint";
        long oldTimestamp = Instant.now().minus(Duration.ofHours(25)).toEpochMilli();
        when(valueOperations.get(anyString())).thenReturn(String.valueOf(oldTimestamp));

        // Act
        Instant nextAllowed = rateLimitService.getNextAllowedSubmissionTime(userFingerprint);

        // Assert
        assertThat(nextAllowed).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("should handle invalid timestamp gracefully")
    void canSubmit_invalidTimestamp_shouldReturnTrue() {
        // Arrange
        String userFingerprint = "test-fingerprint";
        when(valueOperations.get(anyString())).thenReturn("invalid-timestamp");

        // Act
        boolean canSubmit = rateLimitService.canSubmit(userFingerprint);

        // Assert
        assertThat(canSubmit).isTrue();
    }

    @Test
    @DisplayName("should integrate with FingerprintService for request-based rate limiting")
    void canSubmit_withHttpRequest_shouldUseFingerprintService() {
        // Arrange
        String expectedFingerprint = "test-fingerprint-from-request";
        when(fingerprintService.generateFingerprint(request)).thenReturn(expectedFingerprint);
        when(valueOperations.get("rate_limit:" + expectedFingerprint)).thenReturn(null);

        // Act
        boolean canSubmit = rateLimitService.canSubmit(request);

        // Assert
        assertThat(canSubmit).isTrue();
        verify(fingerprintService).generateFingerprint(request);
        verify(valueOperations).get("rate_limit:" + expectedFingerprint);
    }

    @Test
    @DisplayName("should record submission using fingerprint from request")
    void recordSubmission_withHttpRequest_shouldUseFingerprintService() {
        // Arrange
        String expectedFingerprint = "test-fingerprint-from-request";
        when(fingerprintService.generateFingerprint(request)).thenReturn(expectedFingerprint);

        // Act
        rateLimitService.recordSubmission(request);

        // Assert
        verify(fingerprintService).generateFingerprint(request);
        verify(valueOperations).set(eq("rate_limit:" + expectedFingerprint), anyString(), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("should calculate next submission time using fingerprint from request")
    void getNextAllowedSubmissionTime_withHttpRequest_shouldUseFingerprintService() {
        // Arrange
        String expectedFingerprint = "test-fingerprint-from-request";
        String recentTimestamp = String.valueOf(Instant.now().toEpochMilli());
        when(fingerprintService.generateFingerprint(request)).thenReturn(expectedFingerprint);
        when(valueOperations.get("rate_limit:" + expectedFingerprint)).thenReturn(recentTimestamp);

        // Act
        Instant nextAllowed = rateLimitService.getNextAllowedSubmissionTime(request);

        // Assert
        assertThat(nextAllowed).isAfter(Instant.now());
        verify(fingerprintService).generateFingerprint(request);
        verify(valueOperations).get("rate_limit:" + expectedFingerprint);
    }
}
