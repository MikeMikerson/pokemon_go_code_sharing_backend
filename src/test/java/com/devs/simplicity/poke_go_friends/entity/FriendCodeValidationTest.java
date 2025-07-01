package com.devs.simplicity.poke_go_friends.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FriendCode Validation Tests")
class FriendCodeValidationTest {

    private Validator validator;
    private FriendCode friendCode;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid friend code as baseline
        friendCode = new FriendCode();
        friendCode.setFriendCode("123456789012");
        friendCode.setTrainerName("ValidTrainer");
    }

    @Test
    @DisplayName("Valid friend code should pass validation")
    void validFriendCode_shouldPassValidation() {
        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Friend code is required")
    void friendCodeRequired_shouldFailValidation() {
        // Given
        friendCode.setFriendCode(null);

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Friend code is required");
    }

    @Test
    @DisplayName("Friend code cannot be blank")
    void friendCodeBlank_shouldFailValidation() {
        // Given
        friendCode.setFriendCode("");

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(2); // NotBlank and Pattern violations
    }

    @Test
    @DisplayName("Friend code must be exactly 12 digits")
    void friendCodeWrongLength_shouldFailValidation() {
        // Given - too short
        friendCode.setFriendCode("12345");

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Friend code must be exactly 12 digits");

        // Given - too long
        friendCode.setFriendCode("1234567890123");

        // When
        violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Friend code must be exactly 12 digits");
    }

    @Test
    @DisplayName("Friend code must contain only digits")
    void friendCodeNonDigits_shouldFailValidation() {
        // Given
        friendCode.setFriendCode("12345678901a");

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Friend code must be exactly 12 digits");
    }
    @Test
    @DisplayName("Trainer name is optional (null should pass validation)")
    void trainerNameNull_shouldPassValidation() {
        // Given
        friendCode.setTrainerName(null);

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Trainer name can be blank (should pass validation)")
    void trainerNameBlank_shouldPassValidation() {
        // Given
        friendCode.setTrainerName("");

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }


    @Test
    @DisplayName("Valid trainer name sizes should pass validation")
    void trainerNameValidSizes_shouldPassValidation() {
        // Given - minimum length
        friendCode.setTrainerName("AB");

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();

        // Given - maximum length
        friendCode.setTrainerName("A".repeat(100));

        // When
        violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Player level must be between 1 and 50")
    void playerLevelValidation_shouldFailForInvalidLevels() {
        // Given - too low
        friendCode.setPlayerLevel(0);

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Player level must be at least 1");

        // Given - too high
        friendCode.setPlayerLevel(51);

        // When
        violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Player level cannot exceed 50");
    }

    @Test
    @DisplayName("Valid player levels should pass validation")
    void playerLevelValidation_shouldPassForValidLevels() {
        // Given - minimum level
        friendCode.setPlayerLevel(1);

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();

        // Given - maximum level
        friendCode.setPlayerLevel(50);

        // When
        violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();

        // Given - null (optional field)
        friendCode.setPlayerLevel(null);

        // When
        violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Location must not exceed 200 characters")
    void locationSizeValidation_shouldFailForTooLong() {
        // Given
        friendCode.setLocation("A".repeat(201));

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Location cannot exceed 200 characters");
    }

    @Test
    @DisplayName("Valid location sizes should pass validation")
    void locationSizeValidation_shouldPassForValidSizes() {
        // Given - maximum length
        friendCode.setLocation("A".repeat(200));

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();

        // Given - null (optional field)
        friendCode.setLocation(null);

        // When
        violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Description must not exceed 1000 characters")
    void descriptionSizeValidation_shouldFailForTooLong() {
        // Given
        friendCode.setDescription("A".repeat(1001));

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Description cannot exceed 1000 characters");
    }

    @Test
    @DisplayName("Valid description sizes should pass validation")
    void descriptionSizeValidation_shouldPassForValidSizes() {
        // Given - maximum length
        friendCode.setDescription("A".repeat(1000));

        // When
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();

        // Given - null (optional field)
        friendCode.setDescription(null);

        // When
        violations = validator.validate(friendCode);

        // Then
        assertThat(violations).isEmpty();
    }
}
