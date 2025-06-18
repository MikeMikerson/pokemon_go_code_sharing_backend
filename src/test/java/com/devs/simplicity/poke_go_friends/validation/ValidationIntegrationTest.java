package com.devs.simplicity.poke_go_friends.validation;

import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for enhanced validation.
 * Tests the complete validation chain including HTML sanitization.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Enhanced Validation Integration Tests")
class ValidationIntegrationTest {

    private Validator validator;
    private FriendCodeSubmissionRequest.FriendCodeSubmissionRequestBuilder validRequestBuilder;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validRequestBuilder = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012");
    }

    @Test
    @DisplayName("Valid request with all fields should pass validation")
    void validRequest_withAllFields_shouldPassValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerName("CleanTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Request with HTML in trainer name should fail validation")
    void requestWithHtmlInTrainerName_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerName("Trainer<script>alert('xss')</script>")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer name cannot contain HTML or script content");
    }

    @Test
    @DisplayName("Request with HTML in message should fail validation")
    void requestWithHtmlInMessage_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .message("Hello <b>world</b>!")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Message cannot contain HTML or script content");
    }

    @Test
    @DisplayName("Request with script content should fail validation")
    void requestWithScriptContent_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerName("Evil<script>alert('xss')</script>Trainer")
                .message("Innocent message<script>steal_cookies()</script>")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations.stream()
                .map(ConstraintViolation::getMessage))
                .containsExactlyInAnyOrder(
                        "Trainer name cannot contain HTML or script content",
                        "Message cannot contain HTML or script content"
                );
    }

    @Test
    @DisplayName("Request with JavaScript URL should fail validation")
    void requestWithJavaScriptUrl_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .message("Click here: javascript:alert('xss')")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Message cannot contain HTML or script content");
    }

    @Test
    @DisplayName("Request with invalid friend code format should fail validation")
    void requestWithInvalidFriendCode_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .friendCode("123abc789012") // Contains letters
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
    }

    @Test
    @DisplayName("Request with invalid trainer level should fail validation")
    void requestWithInvalidTrainerLevel_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerLevel(51) // Above maximum
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer level cannot exceed 50");
    }

    @Test
    @DisplayName("Request with invalid country should fail validation")
    void requestWithInvalidCountry_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .country("InvalidCountry")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Invalid country code");
    }

    @Test
    @DisplayName("Request with multiple validation errors should report all errors")
    void requestWithMultipleErrors_shouldReportAllErrors() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .friendCode("123") // Too short
                .trainerName("Evil<script>alert('xss')</script>Trainer") // Contains HTML
                .trainerLevel(0) // Below minimum
                .country("FakeCountry") // Invalid country
                .message("Bad message<img src=x onerror=alert('xss')>") // Contains HTML
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(5);
        
        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
        
        assertThat(violationMessages).contains(
                "Friend code must be exactly 12 digits",
                "Trainer name cannot contain HTML or script content",
                "Trainer level must be at least 1",
                "Invalid country code",
                "Message cannot contain HTML or script content"
        );
    }

    @Test
    @DisplayName("Request with extremely long message should fail validation")
    void requestWithExtremelyLongMessage_shouldFailValidation() {
        // Given
        String longMessage = "a".repeat(101); // Exceeds 100 character limit
        FriendCodeSubmissionRequest request = validRequestBuilder
                .message(longMessage)
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Message cannot exceed 100 characters");
    }
}
