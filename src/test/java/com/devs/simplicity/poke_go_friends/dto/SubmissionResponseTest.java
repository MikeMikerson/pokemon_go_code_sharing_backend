package com.devs.simplicity.poke_go_friends.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SubmissionResponse DTO.
 * Tests factory methods and data transfer behavior.
 */
@DisplayName("SubmissionResponse Tests")
class SubmissionResponseTest {

    @Test
    @DisplayName("success factory method should create successful response")
    void success_shouldCreateSuccessfulResponse() {
        // Given
        FriendCodeResponse friendCode = FriendCodeResponse.builder()
                .id(UUID.randomUUID())
                .friendCode("123456789012")
                .build();
        LocalDateTime nextSubmission = LocalDateTime.now().plusHours(24);

        // When
        SubmissionResponse response = SubmissionResponse.success(friendCode, nextSubmission);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Friend code submitted successfully");
        assertThat(response.getFriendCode()).isEqualTo(friendCode);
        assertThat(response.getNextSubmissionAllowed()).isEqualTo(nextSubmission);
    }

    @Test
    @DisplayName("rateLimited factory method should create rate limited response")
    void rateLimited_shouldCreateRateLimitedResponse() {
        // Given
        LocalDateTime nextSubmission = LocalDateTime.now().plusHours(12);

        // When
        SubmissionResponse response = SubmissionResponse.rateLimited(nextSubmission);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Rate limit exceeded. Please try again later.");
        assertThat(response.getFriendCode()).isNull();
        assertThat(response.getNextSubmissionAllowed()).isEqualTo(nextSubmission);
    }

    @Test
    @DisplayName("validationError factory method should create validation error response")
    void validationError_shouldCreateValidationErrorResponse() {
        // Given
        String errorMessage = "Friend code must be exactly 12 digits";

        // When
        SubmissionResponse response = SubmissionResponse.validationError(errorMessage);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(errorMessage);
        assertThat(response.getFriendCode()).isNull();
        assertThat(response.getNextSubmissionAllowed()).isNull();
    }

    @Test
    @DisplayName("Builder should create response with all fields")
    void builder_shouldCreateResponseWithAllFields() {
        // Given
        FriendCodeResponse friendCode = FriendCodeResponse.builder()
                .id(UUID.randomUUID())
                .friendCode("123456789012")
                .build();
        LocalDateTime nextSubmission = LocalDateTime.now().plusHours(24);

        // When
        SubmissionResponse response = SubmissionResponse.builder()
                .success(true)
                .message("Custom message")
                .friendCode(friendCode)
                .nextSubmissionAllowed(nextSubmission)
                .build();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Custom message");
        assertThat(response.getFriendCode()).isEqualTo(friendCode);
        assertThat(response.getNextSubmissionAllowed()).isEqualTo(nextSubmission);
    }

    @Test
    @DisplayName("Data annotation should provide equality and hashCode")
    void dataAnnotation_shouldProvideEqualityAndHashCode() {
        // Given
        FriendCodeResponse friendCode = FriendCodeResponse.builder()
                .id(UUID.randomUUID())
                .friendCode("123456789012")
                .build();
        LocalDateTime nextSubmission = LocalDateTime.now().plusHours(24);

        SubmissionResponse response1 = SubmissionResponse.builder()
                .success(true)
                .message("Test message")
                .friendCode(friendCode)
                .nextSubmissionAllowed(nextSubmission)
                .build();

        SubmissionResponse response2 = SubmissionResponse.builder()
                .success(true)
                .message("Test message")
                .friendCode(friendCode)
                .nextSubmissionAllowed(nextSubmission)
                .build();

        // When & Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Data annotation should provide toString")
    void dataAnnotation_shouldProvideToString() {
        // Given
        SubmissionResponse response = SubmissionResponse.builder()
                .success(true)
                .message("Test message")
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("SubmissionResponse");
        assertThat(toString).contains("success=true");
        assertThat(toString).contains("Test message");
    }

    @Test
    @DisplayName("Builder should create minimal response")
    void builder_shouldCreateMinimalResponse() {
        // Given & When
        SubmissionResponse response = SubmissionResponse.builder()
                .success(false)
                .message("Error occurred")
                .build();

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error occurred");
        assertThat(response.getFriendCode()).isNull();
        assertThat(response.getNextSubmissionAllowed()).isNull();
    }
}
