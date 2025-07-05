package com.devs.simplicity.poke_go_friends.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeSubmissionRequest DTO validation.
 * Tests validation constraints and behavior.
 */
@DisplayName("FriendCodeSubmissionRequest")
class FriendCodeSubmissionRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Valid Requests")
    class ValidRequests {

        @Test
        @DisplayName("should pass validation with all required fields")
        void shouldPassValidationWithRequiredFields() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation with all fields")
        void shouldPassValidationWithAllFields() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", 25, "New York", "Looking for daily gifts");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation with minimum valid values")
        void shouldPassValidationWithMinimumValues() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "000000000000", "AB", 1, "", "");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation with maximum valid values")
        void shouldPassValidationWithMaximumValues() {
            // Arrange
            String longTrainerName = "A".repeat(100);
            String longLocation = "B".repeat(200);
            String longDescription = "C".repeat(1000);
            
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "999999999999", longTrainerName, 50, longLocation, longDescription);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Friend Code Validation")
    class FriendCodeValidation {

        @Test
        @DisplayName("should fail validation when friend code is null")
        void shouldFailValidationWhenFriendCodeIsNull() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                null, "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code is required");
        }

        @Test
        @DisplayName("should fail validation when friend code is empty")
        void shouldFailValidationWhenFriendCodeIsEmpty() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(2);
            assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                    "Friend code is required",
                    "Friend code must be exactly 12 digits");
        }

        @Test
        @DisplayName("should fail validation when friend code is too short")
        void shouldFailValidationWhenFriendCodeTooShort() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "12345", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
        }

        @Test
        @DisplayName("should fail validation when friend code is too long")
        void shouldFailValidationWhenFriendCodeTooLong() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "1234567890123", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
        }

        @Test
        @DisplayName("should fail validation when friend code contains non-digits")
        void shouldFailValidationWhenFriendCodeContainsNonDigits() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "12345678901a", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
        }

        @Test
        @DisplayName("should fail validation when friend code contains spaces")
        void shouldFailValidationWhenFriendCodeContainsSpaces() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "1234 5678 9012", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
        }

        @Test
        @DisplayName("should fail validation when friend code contains dashes")
        void shouldFailValidationWhenFriendCodeContainsDashes() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "1234-5678-9012", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
        }

        @Test
        @DisplayName("should fail validation when friend code contains mixed spaces and dashes")
        void shouldFailValidationWhenFriendCodeContainsMixedSpacesAndDashes() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "1234 5678-9012", "TestTrainer");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
        }
    }

    @Nested
    @DisplayName("Trainer Name Validation")
    class TrainerNameValidation {

        @Test
        @DisplayName("should pass validation when trainer name is null")
        void shouldPassValidationWhenTrainerNameIsNull() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", null);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation when trainer name is empty")
        void shouldPassValidationWhenTrainerNameIsEmpty() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "");

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Player Level Validation")
    class PlayerLevelValidation {

        @Test
        @DisplayName("should pass validation when player level is null")
        void shouldPassValidationWhenPlayerLevelIsNull() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", null, null, null);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should fail validation when player level is too low")
        void shouldFailValidationWhenPlayerLevelTooLow() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", 0, null, null);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Player level must be at least 1");
        }

        @Test
        @DisplayName("should fail validation when player level is too high")
        void shouldFailValidationWhenPlayerLevelTooHigh() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", 51, null, null);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Player level cannot exceed 50");
        }
    }

    @Nested
    @DisplayName("Location Validation")
    class LocationValidation {

        @Test
        @DisplayName("should pass validation when location is null")
        void shouldPassValidationWhenLocationIsNull() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", null, null, null);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should fail validation when location is too long")
        void shouldFailValidationWhenLocationTooLong() {
            // Arrange
            String longLocation = "A".repeat(201);
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", null, longLocation, null);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Location cannot exceed 200 characters");
        }
    }

    @Nested
    @DisplayName("Description Validation")
    class DescriptionValidation {

        @Test
        @DisplayName("should pass validation when description is null")
        void shouldPassValidationWhenDescriptionIsNull() {
            // Arrange
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", null, null, null);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should fail validation when description is too long")
        void shouldFailValidationWhenDescriptionTooLong() {
            // Arrange
            String longDescription = "A".repeat(1001);
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", null, null, longDescription);

            // Act
            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description cannot exceed 1000 characters");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create request with required fields constructor")
        void shouldCreateRequestWithRequiredFieldsConstructor() {
            // Arrange & Act
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer");

            // Assert
            assertThat(request.getFriendCode()).isEqualTo("123456789012");
            assertThat(request.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(request.getPlayerLevel()).isNull();
            assertThat(request.getLocation()).isNull();
            assertThat(request.getDescription()).isNull();
        }

        @Test
        @DisplayName("should create request with all args constructor")
        void shouldCreateRequestWithAllArgsConstructor() {
            // Arrange & Act
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest(
                "123456789012", "TestTrainer", 25, "New York", "Looking for gifts");

            // Assert
            assertThat(request.getFriendCode()).isEqualTo("123456789012");
            assertThat(request.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(request.getPlayerLevel()).isEqualTo(25);
            assertThat(request.getLocation()).isEqualTo("New York");
            assertThat(request.getDescription()).isEqualTo("Looking for gifts");
        }

        @Test
        @DisplayName("should create request with no args constructor")
        void shouldCreateRequestWithNoArgsConstructor() {
            // Arrange & Act
            FriendCodeSubmissionRequest request = new FriendCodeSubmissionRequest();

            // Assert
            assertThat(request.getFriendCode()).isNull();
            assertThat(request.getTrainerName()).isNull();
            assertThat(request.getPlayerLevel()).isNull();
            assertThat(request.getLocation()).isNull();
            assertThat(request.getDescription()).isNull();
        }
    }
}
