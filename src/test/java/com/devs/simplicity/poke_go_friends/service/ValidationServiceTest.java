package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RateLimitConfig;
import com.devs.simplicity.poke_go_friends.exception.RateLimitExceededException;
import com.devs.simplicity.poke_go_friends.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive unit tests for ValidationService.
 */
class ValidationServiceTest {

    private ValidationService validationService;
    
    @Mock
    private RateLimitConfig rateLimitConfig;
    
    @Mock
    private InputSanitizationService sanitizationService;
    
    @Mock
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup default behavior for mocks
        when(rateLimitConfig.isEnabled()).thenReturn(true);
        when(rateLimitConfig.getSubmissionsPerHourPerIp()).thenReturn(5);
        when(rateLimitConfig.getSubmissionsPerDayPerUser()).thenReturn(10);
        
        // Setup sanitization service to return input unchanged by default
        when(sanitizationService.sanitizeTrainerName(anyString())).thenAnswer(i -> i.getArgument(0));
        when(sanitizationService.sanitizeLocation(anyString())).thenAnswer(i -> i.getArgument(0));
        when(sanitizationService.sanitizeDescription(anyString())).thenAnswer(i -> i.getArgument(0));
        when(sanitizationService.isValidAfterSanitization(anyString(), anyString())).thenReturn(true);
        
        // Setup rate limiter to allow requests by default
        when(rateLimiter.isAllowed(anyString())).thenReturn(true);
        
        validationService = new ValidationService(rateLimitConfig, sanitizationService, rateLimiter);
    }

    @Nested
    @DisplayName("Friend Code Format Validation")
    class FriendCodeFormatValidationTest {

        @Test
        @DisplayName("Should accept valid 12-digit friend code")
        void shouldAcceptValidFriendCode() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateFriendCodeFormat("123456789012"));
        }

        @Test
        @DisplayName("Should accept friend codes with spaces and dashes")
        void shouldAcceptFriendCodesWithSpacesAndDashes() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateFriendCodeFormat("123 456 789 012"));
            assertThatNoException().isThrownBy(() -> 
                validationService.validateFriendCodeFormat("123-456-789-012"));
            assertThatNoException().isThrownBy(() -> 
                validationService.validateFriendCodeFormat("1234 5678 9012"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345678901", "1234567890123", "12345678901a", "abcd12345678"})
        @DisplayName("Should reject invalid friend codes")
        void shouldRejectInvalidFriendCodes(String invalidCode) {
            assertThatThrownBy(() -> validationService.validateFriendCodeFormat(invalidCode))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Friend code must be exactly 12 digits");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("Should reject empty friend codes")
        void shouldRejectEmptyFriendCodes(String emptyCode) {
            assertThatThrownBy(() -> validationService.validateFriendCodeFormat(emptyCode))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Friend code cannot be empty");
        }

        @Test
        @DisplayName("Should reject null friend code")
        void shouldRejectNullFriendCode() {
            assertThatThrownBy(() -> validationService.validateFriendCodeFormat(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Friend code cannot be empty");
        }
    }

    @Nested
    @DisplayName("Trainer Name Validation")
    class TrainerNameValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {"Ash", "PikachuMaster", "Trainer123", "TestPlayer", "User1", "ABC123", "MegaTrainer"})
        @DisplayName("Should accept valid trainer names")
        void shouldAcceptValidTrainerNames(String validName) {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateTrainerName(validName));
        }

        @Test
        @DisplayName("Should reject trainer name with inappropriate content")
        void shouldRejectInappropriateTrainerName() {
            assertThatThrownBy(() -> validationService.validateTrainerName("SpamBot123"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("inappropriate content");
        }

        @Test
        @DisplayName("Should reject trainer name that is too long")
        void shouldRejectTooLongTrainerName() {
            String longName = "A".repeat(21);
            assertThatThrownBy(() -> validationService.validateTrainerName(longName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cannot exceed 20 characters");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("Should accept empty or whitespace trainer names (optional field)")
        void shouldAcceptEmptyTrainerNames(String emptyName) {
            assertThatNoException().isThrownBy(() -> validationService.validateTrainerName(emptyName));
        }

        @Test
        @DisplayName("Should accept null trainer name (optional field)")
        void shouldAcceptNullTrainerName() {
            assertThatNoException().isThrownBy(() -> validationService.validateTrainerName(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Trainer@123", "Player#1", "Name$", "Test%", "User&Name", "Red-Blue", "Player.Name", "Team Rocket", "Trainer_123"})
        @DisplayName("Should reject trainer names with invalid characters")
        void shouldRejectInvalidCharacters(String invalidName) {
            assertThatThrownBy(() -> validationService.validateTrainerName(invalidName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("can only contain letters and numbers");
        }
    }

    @Nested
    @DisplayName("Player Level Validation")
    class PlayerLevelValidationTest {

        @Test
        @DisplayName("Should accept null player level (optional field)")
        void shouldAcceptNullPlayerLevel() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validatePlayerLevel(null));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 25, 40, 50})
        @DisplayName("Should accept valid player levels")
        void shouldAcceptValidPlayerLevels(int level) {
            assertThatNoException().isThrownBy(() -> 
                validationService.validatePlayerLevel(level));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, 51, 100})
        @DisplayName("Should reject invalid player levels")
        void shouldRejectInvalidPlayerLevels(int level) {
            assertThatThrownBy(() -> validationService.validatePlayerLevel(level))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("between 1 and 50");
        }
    }

    @Nested
    @DisplayName("Location Validation")
    class LocationValidationTest {

        @Test
        @DisplayName("Should accept null location (optional field)")
        void shouldAcceptNullLocation() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateLocation(null));
        }

        @Test
        @DisplayName("Should accept empty location")
        void shouldAcceptEmptyLocation() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateLocation(""));
        }

        @ParameterizedTest
        @ValueSource(strings = {"New York", "Tokyo, Japan", "London, UK", "Sydney"})
        @DisplayName("Should accept valid locations")
        void shouldAcceptValidLocations(String location) {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateLocation(location));
        }

        @Test
        @DisplayName("Should reject location that is too long")
        void shouldRejectTooLongLocation() {
            String longLocation = "A".repeat(201);
            assertThatThrownBy(() -> validationService.validateLocation(longLocation))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cannot exceed 200 characters");
        }

        @Test
        @DisplayName("Should reject location with inappropriate content")
        void shouldRejectInappropriateLocation() {
            assertThatThrownBy(() -> validationService.validateLocation("Spam City"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("inappropriate content");
        }
    }

    @Nested
    @DisplayName("Description Validation")
    class DescriptionValidationTest {

        @Test
        @DisplayName("Should accept null description (optional field)")
        void shouldAcceptNullDescription() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateDescription(null));
        }

        @Test
        @DisplayName("Should accept empty description")
        void shouldAcceptEmptyDescription() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateDescription(""));
        }

        @Test
        @DisplayName("Should accept valid description")
        void shouldAcceptValidDescription() {
            String description = "Looking for daily gift exchange. Active player from New York.";
            assertThatNoException().isThrownBy(() -> 
                validationService.validateDescription(description));
        }

        @Test
        @DisplayName("Should reject description that is too long")
        void shouldRejectTooLongDescription() {
            String longDescription = "A".repeat(1001);
            assertThatThrownBy(() -> validationService.validateDescription(longDescription))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cannot exceed 1000 characters");
        }

        @Test
        @DisplayName("Should reject description with inappropriate content")
        void shouldRejectInappropriateDescription() {
            assertThatThrownBy(() -> validationService.validateDescription("Want to sell items"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("inappropriate content");
        }
    }

    @Nested
    @DisplayName("Rate Limiting")
    class RateLimitingTest {

        @Test
        @DisplayName("Should allow submissions within IP rate limit")
        void shouldAllowWithinIpRateLimit() {
            String ipAddress = "192.168.1.1";
            
            // Mock rate limiter to allow requests
            when(rateLimiter.isAllowed("ip:" + ipAddress + ":submission")).thenReturn(true);
            
            // Should allow multiple submissions when rate limiter allows
            for (int i = 0; i < 5; i++) {
                assertThatNoException().isThrownBy(() -> 
                    validationService.checkRateLimitByIp(ipAddress));
            }
        }

        @Test
        @DisplayName("Should reject when IP rate limit is exceeded")
        void shouldRejectWhenIpRateLimitExceeded() {
            String ipAddress = "192.168.1.2";
            
            // Mock rate limiter to deny requests (rate limit exceeded)
            when(rateLimiter.isAllowed("ip:" + ipAddress + ":submission")).thenReturn(false);
            
            // Should be rejected when rate limiter denies
            assertThatThrownBy(() -> validationService.checkRateLimitByIp(ipAddress))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded");
        }

        @Test
        @DisplayName("Should allow submissions within user rate limit")
        void shouldAllowWithinUserRateLimit() {
            Long userId = 1L;
            
            // Mock rate limiter to allow requests (we need to handle the cast to RedisRateLimiter)
            RedisRateLimiter mockRedisRateLimiter = mock(RedisRateLimiter.class);
            when(mockRedisRateLimiter.isAllowed(anyString(), anyInt(), anyLong())).thenReturn(true);
            
            // Create a new validation service with the RedisRateLimiter mock
            ValidationService testValidationService = new ValidationService(rateLimitConfig, sanitizationService, mockRedisRateLimiter);
            
            // Should allow multiple submissions when rate limiter allows
            for (int i = 0; i < 10; i++) {
                assertThatNoException().isThrownBy(() -> 
                    testValidationService.checkRateLimitByUser(userId));
            }
        }

        @Test
        @DisplayName("Should reject when user rate limit is exceeded")
        void shouldRejectWhenUserRateLimitExceeded() {
            Long userId = 2L;
            
            // Mock rate limiter to deny requests (rate limit exceeded)
            RedisRateLimiter mockRedisRateLimiter = mock(RedisRateLimiter.class);
            when(mockRedisRateLimiter.isAllowed(anyString(), anyInt(), anyLong())).thenReturn(false);
            
            // Create a new validation service with the RedisRateLimiter mock
            ValidationService testValidationService = new ValidationService(rateLimitConfig, sanitizationService, mockRedisRateLimiter);
            
            // Should be rejected when rate limiter denies
            assertThatThrownBy(() -> testValidationService.checkRateLimitByUser(userId))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded");
        }

        @Test
        @DisplayName("Should allow anonymous submissions (null user ID)")
        void shouldAllowAnonymousSubmissions() {
            assertThatNoException().isThrownBy(() -> 
                validationService.checkRateLimitByUser(null));
        }

        @Test
        @DisplayName("Should track separate limits for different IPs")
        void shouldTrackSeparateLimitsForDifferentIps() {
            String ip1 = "192.168.1.3";
            String ip2 = "192.168.1.4";
            
            // Mock rate limiter to deny first IP but allow second IP
            when(rateLimiter.isAllowed("ip:" + ip1 + ":submission")).thenReturn(false);
            when(rateLimiter.isAllowed("ip:" + ip2 + ":submission")).thenReturn(true);
            
            // First IP should be rejected
            assertThatThrownBy(() -> validationService.checkRateLimitByIp(ip1))
                .isInstanceOf(RateLimitExceededException.class);
            
            // Second IP should still be allowed
            assertThatNoException().isThrownBy(() -> 
                validationService.checkRateLimitByIp(ip2));
        }

        @Test
        @DisplayName("Should get current rate limit usage")
        void shouldGetCurrentRateLimitUsage() {
            String ipAddress = "192.168.1.5";
            
            // Mock RedisRateLimiter to return specific usage counts
            RedisRateLimiter mockRedisRateLimiter = mock(RedisRateLimiter.class);
            when(mockRedisRateLimiter.getCurrentUsage("ip:" + ipAddress + ":submission")).thenReturn(0L, 1L, 2L);
            
            // Create a new validation service with the RedisRateLimiter mock
            ValidationService testValidationService = new ValidationService(rateLimitConfig, sanitizationService, mockRedisRateLimiter);
            
            assertThat(testValidationService.getCurrentRateLimitUsage(ipAddress)).isEqualTo(0);
            assertThat(testValidationService.getCurrentRateLimitUsage(ipAddress)).isEqualTo(1);
            assertThat(testValidationService.getCurrentRateLimitUsage(ipAddress)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Complete Submission Validation")
    class CompleteSubmissionValidationTest {

        @Test
        @DisplayName("Should validate complete valid submission")
        void shouldValidateCompleteValidSubmission() {
            assertThatNoException().isThrownBy(() -> 
                validationService.validateFriendCodeSubmission(
                    "123456789012", "TestTrainer", 25, "New York", 
                    "Looking for friends", "192.168.1.10", 1L));
        }

        @Test
        @DisplayName("Should reject submission with invalid friend code")
        void shouldRejectSubmissionWithInvalidFriendCode() {
            assertThatThrownBy(() -> 
                validationService.validateFriendCodeSubmission(
                    "invalid", "TestTrainer", 25, "New York", 
                    "Looking for friends", "192.168.1.11", 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Friend code must be exactly 12 digits");
        }

        @Test
        @DisplayName("Should reject submission with inappropriate trainer name")
        void shouldRejectSubmissionWithInappropriateTrainerName() {
            assertThatThrownBy(() -> 
                validationService.validateFriendCodeSubmission(
                    "123456789012", "SpamBot", 25, "New York", 
                    "Looking for friends", "192.168.1.12", 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("inappropriate content");
        }

        @Test
        @DisplayName("Should reject submission when rate limit exceeded")
        void shouldRejectSubmissionWhenRateLimitExceeded() {
            String ipAddress = "192.168.1.13";
            Long userId = 10L;
            
            // Mock rate limiter to deny IP requests (rate limit exceeded)
            when(rateLimiter.isAllowed("ip:" + ipAddress + ":submission")).thenReturn(false);
            
            // Next submission should be rejected due to IP rate limit
            assertThatThrownBy(() -> 
                validationService.validateFriendCodeSubmission(
                    "123456789015", "TestTrainer", 25, "New York", 
                    "Looking for friends", ipAddress, userId))
                .isInstanceOf(RateLimitExceededException.class);
        }
    }

    @Nested
    @DisplayName("Rate Limit Cleanup")
    class RateLimitCleanupTest {

        @Test
        @DisplayName("Should clean up rate limit data")
        void shouldCleanUpRateLimitData() {
            // With Redis-based rate limiting, cleanup is handled automatically
            // This test just ensures the method doesn't throw exceptions
            assertThatNoException().isThrownBy(() -> validationService.cleanupRateLimitData());
        }
    }
}
