package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeSubmissionRequest DTO.
 * Tests validation annotations and business rules.
 */
@DisplayName("FriendCodeSubmissionRequest Tests")
class FriendCodeSubmissionRequestTest {

    private Validator validator;
    private FriendCodeSubmissionRequest.FriendCodeSubmissionRequestBuilder validRequestBuilder;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validRequestBuilder = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012");
    }

    @Test
    @DisplayName("Valid friend code submission request should pass validation")
    void validRequest_shouldPassValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder.build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Friend code submission request with all optional fields should pass validation")
    void requestWithAllFields_shouldPassValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerName("TestTrainer")
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
    @DisplayName("Null friend code should fail validation")
    void nullFriendCode_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .friendCode(null)
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code is required");
    }

    @Test
    @DisplayName("Empty friend code should fail validation")
    void emptyFriendCode_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .friendCode("")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations.stream()
                .map(ConstraintViolation::getMessage))
                .containsExactlyInAnyOrder(
                        "Friend code is required",
                        "Friend code must be exactly 12 digits"
                );
    }

    @Test
    @DisplayName("Friend code with invalid format should fail validation")
    void invalidFriendCodeFormat_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .friendCode("123abc789012")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
    }

    @Test
    @DisplayName("Friend code with wrong length should fail validation")
    void wrongLengthFriendCode_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .friendCode("12345678901")  // 11 digits
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Friend code must be exactly 12 digits");
    }

    @Test
    @DisplayName("Trainer name exceeding maximum length should fail validation")
    void trainerNameTooLong_shouldFailValidation() {
        // Given
        String longName = "a".repeat(51);
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerName(longName)
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer name cannot exceed 50 characters");
    }

    @Test
    @DisplayName("Trainer level below minimum should fail validation")
    void trainerLevelBelowMinimum_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerLevel(0)
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer level must be at least 1");
    }

    @Test
    @DisplayName("Trainer level above maximum should fail validation")
    void trainerLevelAboveMaximum_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .trainerLevel(51)
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Trainer level cannot exceed 50");
    }

    @Test
    @DisplayName("Invalid country should fail validation")
    void invalidCountry_shouldFailValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .country("Invalid Country")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Invalid country code");
    }

    @Test
    @DisplayName("Valid country should pass validation")
    void validCountry_shouldPassValidation() {
        // Given
        FriendCodeSubmissionRequest request = validRequestBuilder
                .country("United States")
                .build();

        // When
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Message exceeding maximum length should fail validation")
    void messageTooLong_shouldFailValidation() {
        // Given
        String longMessage = "a".repeat(101);
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

    @Test
    @DisplayName("Valid team values should pass validation")
    void validTeam_shouldPassValidation() {
        // Given & When & Then
        for (Team team : Team.values()) {
            FriendCodeSubmissionRequest request = validRequestBuilder
                    .team(team)
                    .build();

            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Valid purpose values should pass validation")
    void validPurpose_shouldPassValidation() {
        // Given & When & Then
        for (Purpose purpose : Purpose.values()) {
            FriendCodeSubmissionRequest request = validRequestBuilder
                    .purpose(purpose)
                    .build();

            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Builder should create request with all fields")
    void builder_shouldCreateRequestWithAllFields() {
        // Given & When
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .build();

        // Then
        assertThat(request.getFriendCode()).isEqualTo("123456789012");
        assertThat(request.getTrainerName()).isEqualTo("TestTrainer");
        assertThat(request.getTrainerLevel()).isEqualTo(25);
        assertThat(request.getTeam()).isEqualTo(Team.MYSTIC);
        assertThat(request.getCountry()).isEqualTo("United States");
        assertThat(request.getPurpose()).isEqualTo(Purpose.BOTH);
        assertThat(request.getMessage()).isEqualTo("Looking for active friends!");
    }
}
