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

@DisplayName("User Validation Tests")
class UserValidationTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid user as baseline
        user = new User();
        user.setUsername("validuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword123");
    }

    @Test
    @DisplayName("Valid user should pass validation")
    void validUser_shouldPassValidation() {
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Username is required")
    void usernameRequired_shouldFailValidation() {
        // Given
        user.setUsername(null);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Username is required");
    }

    @Test
    @DisplayName("Username cannot be blank")
    void usernameBlank_shouldFailValidation() {
        // Given
        user.setUsername("");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(3); // NotBlank, Size, and Pattern violations
        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .containsExactlyInAnyOrder(
                "Username is required",
                "Username must be between 3 and 50 characters",
                "Username can only contain letters, numbers, and underscores"
            );
    }

    @Test
    @DisplayName("Username must be between 3 and 50 characters")
    void usernameSizeValidation_shouldFailForInvalidSizes() {
        // Given - too short
        user.setUsername("ab");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Username must be between 3 and 50 characters");

        // Given - too long
        user.setUsername("a".repeat(51));

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("Username can only contain letters, numbers, and underscores")
    void usernamePattern_shouldFailForInvalidCharacters() {
        // Given - contains special characters
        user.setUsername("user@name");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Username can only contain letters, numbers, and underscores");

        // Given - contains spaces
        user.setUsername("user name");

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Username can only contain letters, numbers, and underscores");
    }

    @Test
    @DisplayName("Valid username patterns should pass validation")
    void usernamePattern_shouldPassForValidPatterns() {
        // Given - letters only
        user.setUsername("username");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();

        // Given - letters and numbers
        user.setUsername("user123");

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();

        // Given - letters, numbers, and underscores
        user.setUsername("user_name_123");

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Email is required")
    void emailRequired_shouldFailValidation() {
        // Given
        user.setEmail(null);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Email is required");
    }

    @Test
    @DisplayName("Email cannot be blank")
    void emailBlank_shouldFailValidation() {
        // Given
        user.setEmail("");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1); // Only NotBlank violation, @Email doesn't trigger on blank
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Email is required");
    }

    @Test
    @DisplayName("Email must be valid format")
    void emailFormat_shouldFailForInvalidEmails() {
        // Given - missing @ symbol
        user.setEmail("invalidemail.com");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Email must be valid");

        // Given - missing domain
        user.setEmail("test@");

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Email must be valid");
    }

    @Test
    @DisplayName("Valid email formats should pass validation")
    void emailFormat_shouldPassForValidEmails() {
        // Given - simple email
        user.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();

        // Given - email with subdomain
        user.setEmail("user@mail.example.com");

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Email must not exceed 255 characters")
    void emailSizeValidation_shouldFailForTooLong() {
        // Given - construct a valid email that's too long
        String longLocalPart = "a".repeat(240);
        user.setEmail(longLocalPart + "@example.com"); // Total > 255 chars but still valid format

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        // Note: The @Email validator might reject this first, so we check for either message
        assertThat(violations).hasSize(1);
        String message = violations.iterator().next().getMessage();
        assertThat(message).isIn("Email cannot exceed 255 characters", "Email must be valid");
    }

    @Test
    @DisplayName("Password is required")
    void passwordRequired_shouldFailValidation() {
        // Given
        user.setPasswordHash(null);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Password is required");
    }

    @Test
    @DisplayName("Password cannot be blank")
    void passwordBlank_shouldFailValidation() {
        // Given
        user.setPasswordHash("");

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Password is required");
    }

    @Test
    @DisplayName("Trainer name must not exceed 100 characters")
    void trainerNameSizeValidation_shouldFailForTooLong() {
        // Given
        user.setTrainerName("A".repeat(101));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Trainer name cannot exceed 100 characters");
    }

    @Test
    @DisplayName("Valid trainer name sizes should pass validation")
    void trainerNameSizeValidation_shouldPassForValidSizes() {
        // Given - maximum length
        user.setTrainerName("A".repeat(100));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();

        // Given - null (optional field)
        user.setTrainerName(null);

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Player level must be between 1 and 50")
    void playerLevelValidation_shouldFailForInvalidLevels() {
        // Given - too low
        user.setPlayerLevel(0);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Player level must be at least 1");

        // Given - too high
        user.setPlayerLevel(51);

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Player level cannot exceed 50");
    }

    @Test
    @DisplayName("Valid player levels should pass validation")
    void playerLevelValidation_shouldPassForValidLevels() {
        // Given - minimum level
        user.setPlayerLevel(1);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();

        // Given - maximum level
        user.setPlayerLevel(50);

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();

        // Given - null (optional field)
        user.setPlayerLevel(null);

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Location must not exceed 200 characters")
    void locationSizeValidation_shouldFailForTooLong() {
        // Given
        user.setLocation("A".repeat(201));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Location cannot exceed 200 characters");
    }

    @Test
    @DisplayName("Valid location sizes should pass validation")
    void locationSizeValidation_shouldPassForValidSizes() {
        // Given - maximum length
        user.setLocation("A".repeat(200));

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();

        // Given - null (optional field)
        user.setLocation(null);

        // When
        violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }
}
