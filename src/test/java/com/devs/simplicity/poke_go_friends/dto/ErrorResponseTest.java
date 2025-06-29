package com.devs.simplicity.poke_go_friends.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorResponse DTO.
 * Tests error response creation and factory methods.
 */
@DisplayName("ErrorResponse")
class ErrorResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create error response with basic constructor")
        void shouldCreateErrorResponseWithBasicConstructor() {
            // Arrange & Act
            ErrorResponse response = new ErrorResponse(400, "Bad Request", "Invalid input");

            // Assert
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getError()).isEqualTo("Bad Request");
            assertThat(response.getMessage()).isEqualTo("Invalid input");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
        }

        @Test
        @DisplayName("should create error response with path constructor")
        void shouldCreateErrorResponseWithPathConstructor() {
            // Arrange & Act
            ErrorResponse response = new ErrorResponse(404, "Not Found", "Resource not found", "/api/friend-codes/999");

            // Assert
            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(response.getError()).isEqualTo("Not Found");
            assertThat(response.getMessage()).isEqualTo("Resource not found");
            assertThat(response.getPath()).isEqualTo("/api/friend-codes/999");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should create error response with full details constructor")
        void shouldCreateErrorResponseWithFullDetailsConstructor() {
            // Arrange & Act
            ErrorResponse response = new ErrorResponse(
                500, 
                "Internal Server Error", 
                "Database connection failed",
                "Connection timeout after 30 seconds",
                "/api/friend-codes"
            );

            // Assert
            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getError()).isEqualTo("Internal Server Error");
            assertThat(response.getMessage()).isEqualTo("Database connection failed");
            assertThat(response.getDetails()).isEqualTo("Connection timeout after 30 seconds");
            assertThat(response.getPath()).isEqualTo("/api/friend-codes");
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should create error response with all args constructor")
        void shouldCreateErrorResponseWithAllArgsConstructor() {
            // Arrange
            LocalDateTime specificTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

            // Act
            ErrorResponse response = new ErrorResponse(
                422, 
                "Unprocessable Entity", 
                "Validation failed",
                "Friend code format is invalid",
                specificTime,
                "/api/friend-codes"
            );

            // Assert
            assertThat(response.getStatus()).isEqualTo(422);
            assertThat(response.getError()).isEqualTo("Unprocessable Entity");
            assertThat(response.getMessage()).isEqualTo("Validation failed");
            assertThat(response.getDetails()).isEqualTo("Friend code format is invalid");
            assertThat(response.getTimestamp()).isEqualTo(specificTime);
            assertThat(response.getPath()).isEqualTo("/api/friend-codes");
        }

        @Test
        @DisplayName("should create error response with no args constructor")
        void shouldCreateErrorResponseWithNoArgsConstructor() {
            // Arrange & Act
            ErrorResponse response = new ErrorResponse();

            // Assert
            assertThat(response.getStatus()).isEqualTo(0);
            assertThat(response.getError()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getDetails()).isNull();
            assertThat(response.getTimestamp()).isNull();
            assertThat(response.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should create bad request error response")
        void shouldCreateBadRequestErrorResponse() {
            // Arrange & Act
            ErrorResponse response = ErrorResponse.badRequest("Invalid friend code format");

            // Assert
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getError()).isEqualTo("Bad Request");
            assertThat(response.getMessage()).isEqualTo("Invalid friend code format");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should create not found error response")
        void shouldCreateNotFoundErrorResponse() {
            // Arrange & Act
            ErrorResponse response = ErrorResponse.notFound("Friend code not found");

            // Assert
            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(response.getError()).isEqualTo("Not Found");
            assertThat(response.getMessage()).isEqualTo("Friend code not found");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should create conflict error response")
        void shouldCreateConflictErrorResponse() {
            // Arrange & Act
            ErrorResponse response = ErrorResponse.conflict("Friend code already exists");

            // Assert
            assertThat(response.getStatus()).isEqualTo(409);
            assertThat(response.getError()).isEqualTo("Conflict");
            assertThat(response.getMessage()).isEqualTo("Friend code already exists");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should create too many requests error response")
        void shouldCreateTooManyRequestsErrorResponse() {
            // Arrange & Act
            ErrorResponse response = ErrorResponse.tooManyRequests("Rate limit exceeded");

            // Assert
            assertThat(response.getStatus()).isEqualTo(429);
            assertThat(response.getError()).isEqualTo("Too Many Requests");
            assertThat(response.getMessage()).isEqualTo("Rate limit exceeded");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should create internal server error response")
        void shouldCreateInternalServerErrorResponse() {
            // Arrange & Act
            ErrorResponse response = ErrorResponse.internalServerError("Database connection failed");

            // Assert
            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getError()).isEqualTo("Internal Server Error");
            assertThat(response.getMessage()).isEqualTo("Database connection failed");
            assertThat(response.getDetails()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Timestamp Behavior Tests")
    class TimestampBehaviorTests {

        @Test
        @DisplayName("should set timestamp automatically when using basic constructor")
        void shouldSetTimestampAutomaticallyWhenUsingBasicConstructor() {
            // Arrange
            LocalDateTime beforeCreation = LocalDateTime.now();

            // Act
            ErrorResponse response = new ErrorResponse(400, "Bad Request", "Test message");
            
            // Assert
            LocalDateTime afterCreation = LocalDateTime.now();
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getTimestamp()).isAfterOrEqualTo(beforeCreation);
            assertThat(response.getTimestamp()).isBeforeOrEqualTo(afterCreation);
        }

        @Test
        @DisplayName("should set timestamp automatically when using path constructor")
        void shouldSetTimestampAutomaticallyWhenUsingPathConstructor() {
            // Arrange
            LocalDateTime beforeCreation = LocalDateTime.now();

            // Act
            ErrorResponse response = new ErrorResponse(404, "Not Found", "Test message", "/test/path");
            
            // Assert
            LocalDateTime afterCreation = LocalDateTime.now();
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getTimestamp()).isAfterOrEqualTo(beforeCreation);
            assertThat(response.getTimestamp()).isBeforeOrEqualTo(afterCreation);
        }

        @Test
        @DisplayName("should set timestamp automatically when using factory methods")
        void shouldSetTimestampAutomaticallyWhenUsingFactoryMethods() {
            // Arrange
            LocalDateTime beforeCreation = LocalDateTime.now();

            // Act
            ErrorResponse response = ErrorResponse.badRequest("Test message");
            
            // Assert
            LocalDateTime afterCreation = LocalDateTime.now();
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getTimestamp()).isAfterOrEqualTo(beforeCreation);
            assertThat(response.getTimestamp()).isBeforeOrEqualTo(afterCreation);
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("should preserve all data when using full constructor")
        void shouldPreserveAllDataWhenUsingFullConstructor() {
            // Arrange
            LocalDateTime specificTime = LocalDateTime.of(2023, 6, 15, 14, 30, 45);

            // Act
            ErrorResponse response = new ErrorResponse(
                422,
                "Unprocessable Entity",
                "Validation failed on multiple fields",
                "Friend code must be 12 digits, trainer name is required",
                specificTime,
                "/api/friend-codes"
            );

            // Assert
            assertThat(response.getStatus()).isEqualTo(422);
            assertThat(response.getError()).isEqualTo("Unprocessable Entity");
            assertThat(response.getMessage()).isEqualTo("Validation failed on multiple fields");
            assertThat(response.getDetails()).isEqualTo("Friend code must be 12 digits, trainer name is required");
            assertThat(response.getTimestamp()).isEqualTo(specificTime);
            assertThat(response.getPath()).isEqualTo("/api/friend-codes");
        }

        @Test
        @DisplayName("should handle null values correctly")
        void shouldHandleNullValuesCorrectly() {
            // Arrange & Act
            ErrorResponse response = new ErrorResponse(400, null, null, null, null);

            // Assert
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getError()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getDetails()).isNull();
            assertThat(response.getPath()).isNull();
            assertThat(response.getTimestamp()).isNotNull(); // Timestamp is set automatically
        }

        @Test
        @DisplayName("should handle empty strings correctly")
        void shouldHandleEmptyStringsCorrectly() {
            // Arrange & Act
            ErrorResponse response = new ErrorResponse(400, "", "", "", "");

            // Assert
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getError()).isEmpty();
            assertThat(response.getMessage()).isEmpty();
            assertThat(response.getDetails()).isEmpty();
            assertThat(response.getPath()).isEmpty();
            assertThat(response.getTimestamp()).isNotNull(); // Timestamp is set automatically
        }
    }
}
