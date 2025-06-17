package com.devs.simplicity.poke_go_friends.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the FriendCode entity.
 * Tests validation rules and business logic methods.
 */
@DisplayName("FriendCode Entity Tests")
class FriendCodeTest {

    private Validator validator;
    private FriendCode.FriendCodeBuilder validFriendCodeBuilder;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validFriendCodeBuilder = FriendCode.builder()
                .friendCode("123456789012")
                .userFingerprint("test-fingerprint-hash");
    }

    @Test
    @DisplayName("Valid friend code should pass validation")
    void validFriendCode_shouldPassValidation() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder.build();

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Friend code with invalid format should fail validation")
    void invalidFriendCodeFormat_shouldFailValidation() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .friendCode("123abc789012")
                .build();

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
    }

    @Test
    @DisplayName("Friend code with wrong length should fail validation")
    void wrongLengthFriendCode_shouldFailValidation() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .friendCode("12345678901")  // 11 digits
                .build();

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
    }

    @Test
    @DisplayName("Trainer level below minimum should fail validation")
    void trainerLevelBelowMinimum_shouldFailValidation() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .trainerLevel(0)
                .build();

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer level must be at least 1");
    }

    @Test
    @DisplayName("Trainer level above maximum should fail validation")
    void trainerLevelAboveMaximum_shouldFailValidation() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .trainerLevel(51)
                .build();

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer level cannot exceed 50");
    }

    @Test
    @DisplayName("Trainer name exceeding maximum length should fail validation")
    void trainerNameTooLong_shouldFailValidation() {
        // Given
        String longName = "a".repeat(51);
        FriendCode friendCode = validFriendCodeBuilder
                .trainerName(longName)
                .build();

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer name cannot exceed 50 characters");
    }

    @Test
    @DisplayName("Message exceeding maximum length should fail validation")
    void messageTooLong_shouldFailValidation() {
        // Given
        String longMessage = "a".repeat(101);
        FriendCode friendCode = validFriendCodeBuilder
                .message(longMessage)
                .build();

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Message cannot exceed 100 characters");
    }

    @Test
    @DisplayName("isExpired should return true when current time is after expiration")
    void isExpired_shouldReturnTrueWhenExpired() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        // When & Then
        assertThat(friendCode.isExpired()).isTrue();
    }

    @Test
    @DisplayName("isExpired should return false when current time is before expiration")
    void isExpired_shouldReturnFalseWhenNotExpired() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        // When & Then
        assertThat(friendCode.isExpired()).isFalse();
    }

    @Test
    @DisplayName("isActive should return true when not expired")
    void isActive_shouldReturnTrueWhenNotExpired() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        // When & Then
        assertThat(friendCode.isActive()).isTrue();
    }

    @Test
    @DisplayName("isActive should return false when expired")
    void isActive_shouldReturnFalseWhenExpired() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        // When & Then
        assertThat(friendCode.isActive()).isFalse();
    }

    @Test
    @DisplayName("onCreate should set submission time and expiration when not provided")
    void onCreate_shouldSetTimestampsWhenNotProvided() {
        // Given
        FriendCode friendCode = validFriendCodeBuilder.build();
        LocalDateTime beforeCreation = LocalDateTime.now();

        // When
        friendCode.onCreate();

        // Then
        LocalDateTime afterCreation = LocalDateTime.now();
        assertThat(friendCode.getSubmittedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(friendCode.getExpiresAt()).isEqualTo(friendCode.getSubmittedAt().plusHours(48));
    }

    @Test
    @DisplayName("onCreate should not override existing timestamps")
    void onCreate_shouldNotOverrideExistingTimestamps() {
        // Given
        LocalDateTime existingSubmission = LocalDateTime.now().minusHours(2);
        LocalDateTime existingExpiration = LocalDateTime.now().plusHours(46);
        
        FriendCode friendCode = validFriendCodeBuilder
                .submittedAt(existingSubmission)
                .expiresAt(existingExpiration)
                .build();

        // When
        friendCode.onCreate();

        // Then
        assertThat(friendCode.getSubmittedAt()).isEqualTo(existingSubmission);
        assertThat(friendCode.getExpiresAt()).isEqualTo(existingExpiration);
    }

    @Test
    @DisplayName("Builder should create valid friend code with all optional fields")
    void builder_shouldCreateValidFriendCodeWithAllFields() {
        // Given & When
        FriendCode friendCode = FriendCode.builder()
                .id(UUID.randomUUID())
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for daily gift exchange!")
                .submittedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(48))
                .userFingerprint("test-fingerprint")
                .build();

        // Then
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);
        assertThat(violations).isEmpty();
        assertThat(friendCode.getFriendCode()).isEqualTo("123456789012");
        assertThat(friendCode.getTrainerName()).isEqualTo("TestTrainer");
        assertThat(friendCode.getTrainerLevel()).isEqualTo(25);
        assertThat(friendCode.getTeam()).isEqualTo(Team.MYSTIC);
        assertThat(friendCode.getCountry()).isEqualTo("United States");
        assertThat(friendCode.getPurpose()).isEqualTo(Purpose.BOTH);
        assertThat(friendCode.getMessage()).isEqualTo("Looking for daily gift exchange!");
    }
}
