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
 * Unit tests for FriendCodeUpdateRequest DTO validation.
 * Tests validation constraints and update detection behavior.
 */
@DisplayName("FriendCodeUpdateRequest")
class FriendCodeUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Valid Update Requests")
    class ValidUpdateRequests {

        @Test
        @DisplayName("should pass validation with all fields")
        void shouldPassValidationWithAllFields() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest(
                "UpdatedTrainer", 35, "Updated Location", "Updated description");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should pass validation with only trainer name")
        void shouldPassValidationWithOnlyTrainerName() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setTrainerName("NewTrainerName");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should pass validation with only player level")
        void shouldPassValidationWithOnlyPlayerLevel() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setPlayerLevel(40);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should pass validation with only location")
        void shouldPassValidationWithOnlyLocation() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setLocation("Paris");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should pass validation with only description")
        void shouldPassValidationWithOnlyDescription() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setDescription("Looking for new friends");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should pass validation with minimum valid values")
        void shouldPassValidationWithMinimumValues() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest(
                "AB", 1, "", "");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should pass validation with maximum valid values")
        void shouldPassValidationWithMaximumValues() {
            // Arrange
            String longTrainerName = "A".repeat(100);
            String longLocation = "B".repeat(200);
            String longDescription = "C".repeat(1000);
            
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest(
                longTrainerName, 50, longLocation, longDescription);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
            assertThat(request.hasAnyUpdate()).isTrue();
        }
    }

    @Nested
    @DisplayName("Trainer Name Validation")
    class TrainerNameValidation {

        @Test
        @DisplayName("should pass validation when trainer name is null")
        void shouldPassValidationWhenTrainerNameIsNull() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest(
                null, 25, "Location", "Description");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should fail validation when trainer name is too short")
        void shouldFailValidationWhenTrainerNameTooShort() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setTrainerName("A");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer name must be between 2 and 100 characters");
        }

        @Test
        @DisplayName("should fail validation when trainer name is too long")
        void shouldFailValidationWhenTrainerNameTooLong() {
            // Arrange
            String longName = "A".repeat(101);
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setTrainerName(longName);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer name must be between 2 and 100 characters");
        }
    }

    @Nested
    @DisplayName("Player Level Validation")
    class PlayerLevelValidation {

        @Test
        @DisplayName("should pass validation when player level is null")
        void shouldPassValidationWhenPlayerLevelIsNull() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setPlayerLevel(null);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should fail validation when player level is too low")
        void shouldFailValidationWhenPlayerLevelTooLow() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setPlayerLevel(0);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Player level must be at least 1");
        }

        @Test
        @DisplayName("should fail validation when player level is too high")
        void shouldFailValidationWhenPlayerLevelTooHigh() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setPlayerLevel(51);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

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
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setLocation(null);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation when location is empty")
        void shouldPassValidationWhenLocationIsEmpty() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setLocation("");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should fail validation when location is too long")
        void shouldFailValidationWhenLocationTooLong() {
            // Arrange
            String longLocation = "A".repeat(201);
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setLocation(longLocation);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

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
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setDescription(null);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation when description is empty")
        void shouldPassValidationWhenDescriptionIsEmpty() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setDescription("");

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should fail validation when description is too long")
        void shouldFailValidationWhenDescriptionTooLong() {
            // Arrange
            String longDescription = "A".repeat(1001);
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setDescription(longDescription);

            // Act
            Set<ConstraintViolation<FriendCodeUpdateRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description cannot exceed 1000 characters");
        }
    }

    @Nested
    @DisplayName("Update Detection Tests")
    class UpdateDetectionTests {

        @Test
        @DisplayName("should detect no updates when all fields are null")
        void shouldDetectNoUpdatesWhenAllFieldsAreNull() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();

            // Act & Assert
            assertThat(request.hasAnyUpdate()).isFalse();
        }

        @Test
        @DisplayName("should detect update when trainer name is provided")
        void shouldDetectUpdateWhenTrainerNameIsProvided() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setTrainerName("NewName");

            // Act & Assert
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should detect update when player level is provided")
        void shouldDetectUpdateWhenPlayerLevelIsProvided() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setPlayerLevel(30);

            // Act & Assert
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should detect update when location is provided")
        void shouldDetectUpdateWhenLocationIsProvided() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setLocation("New York");

            // Act & Assert
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should detect update when description is provided")
        void shouldDetectUpdateWhenDescriptionIsProvided() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setDescription("New description");

            // Act & Assert
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should detect update when empty string is provided for location")
        void shouldDetectUpdateWhenEmptyStringIsProvidedForLocation() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setLocation("");

            // Act & Assert
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should detect update when empty string is provided for description")
        void shouldDetectUpdateWhenEmptyStringIsProvidedForDescription() {
            // Arrange
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();
            request.setDescription("");

            // Act & Assert
            assertThat(request.hasAnyUpdate()).isTrue();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create request with all args constructor")
        void shouldCreateRequestWithAllArgsConstructor() {
            // Arrange & Act
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest(
                "TestTrainer", 25, "New York", "Looking for gifts");

            // Assert
            assertThat(request.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(request.getPlayerLevel()).isEqualTo(25);
            assertThat(request.getLocation()).isEqualTo("New York");
            assertThat(request.getDescription()).isEqualTo("Looking for gifts");
            assertThat(request.hasAnyUpdate()).isTrue();
        }

        @Test
        @DisplayName("should create request with no args constructor")
        void shouldCreateRequestWithNoArgsConstructor() {
            // Arrange & Act
            FriendCodeUpdateRequest request = new FriendCodeUpdateRequest();

            // Assert
            assertThat(request.getTrainerName()).isNull();
            assertThat(request.getPlayerLevel()).isNull();
            assertThat(request.getLocation()).isNull();
            assertThat(request.getDescription()).isNull();
            assertThat(request.hasAnyUpdate()).isFalse();
        }
    }
}
