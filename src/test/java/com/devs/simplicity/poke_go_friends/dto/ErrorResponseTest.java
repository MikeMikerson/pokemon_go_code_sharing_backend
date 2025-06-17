package com.devs.simplicity.poke_go_friends.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorResponse DTO.
 * Tests factory methods and error handling behavior.
 */
@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    @Test
    @DisplayName("of factory method should create simple error response")
    void of_shouldCreateSimpleErrorResponse() {
        // Given
        int status = 400;
        String error = "Bad Request";
        String message = "Invalid input";
        String path = "/api/friend-codes";

        // When
        ErrorResponse response = ErrorResponse.of(status, error, message, path);

        // Then
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getDetails()).isNull();
    }

    @Test
    @DisplayName("validationError factory method should create validation error response")
    void validationError_shouldCreateValidationErrorResponse() {
        // Given
        int status = 400;
        String error = "Validation Failed";
        String message = "Request validation failed";
        String path = "/api/friend-codes";
        List<String> details = List.of(
                "Friend code must be exactly 12 digits",
                "Trainer name cannot exceed 50 characters"
        );

        // When
        ErrorResponse response = ErrorResponse.validationError(status, error, message, path, details);

        // Then
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("rateLimitExceeded factory method should create rate limit error")
    void rateLimitExceeded_shouldCreateRateLimitError() {
        // Given
        String path = "/api/friend-codes";

        // When
        ErrorResponse response = ErrorResponse.rateLimitExceeded(path);

        // Then
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getError()).isEqualTo("Rate Limit Exceeded");
        assertThat(response.getMessage()).isEqualTo("Too many submissions. Please try again later.");
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("badRequest factory method should create bad request error")
    void badRequest_shouldCreateBadRequestError() {
        // Given
        String message = "Invalid friend code format";
        String path = "/api/friend-codes";

        // When
        ErrorResponse response = ErrorResponse.badRequest(message, path);

        // Then
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Bad Request");
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("internalServerError factory method should create internal server error")
    void internalServerError_shouldCreateInternalServerError() {
        // Given
        String path = "/api/friend-codes";

        // When
        ErrorResponse response = ErrorResponse.internalServerError(path);

        // Then
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getError()).isEqualTo("Internal Server Error");
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Builder should create response with all fields")
    void builder_shouldCreateResponseWithAllFields() {
        // Given
        int status = 422;
        String error = "Unprocessable Entity";
        String message = "Business rule violation";
        String path = "/api/friend-codes";
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(5);
        List<String> details = List.of("Rate limit exceeded");

        // When
        ErrorResponse response = ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(timestamp)
                .details(details)
                .build();

        // Then
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getPath()).isEqualTo(path);
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("Data annotation should provide equality and hashCode")
    void dataAnnotation_shouldProvideEqualityAndHashCode() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        ErrorResponse response1 = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Test error")
                .path("/test")
                .timestamp(fixedTime)
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Test error")
                .path("/test")
                .timestamp(fixedTime)
                .build();

        // When & Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Data annotation should provide toString")
    void dataAnnotation_shouldProvideToString() {
        // Given
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .path("/api/test")
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("ErrorResponse");
        assertThat(toString).contains("status=404");
        assertThat(toString).contains("Not Found");
        assertThat(toString).contains("Resource not found");
    }

    @Test
    @DisplayName("Timestamp should be automatically set by factory methods")
    void timestamp_shouldBeAutomaticallySetByFactoryMethods() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // When
        ErrorResponse response = ErrorResponse.of(400, "Bad Request", "Test", "/test");
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(response.getTimestamp()).isAfter(before);
        assertThat(response.getTimestamp()).isBefore(after);
    }

    @Test
    @DisplayName("Builder should create minimal response")
    void builder_shouldCreateMinimalResponse() {
        // Given & When
        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .build();

        // Then
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getError()).isEqualTo("Internal Server Error");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getPath()).isNull();
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getDetails()).isNull();
    }
}
