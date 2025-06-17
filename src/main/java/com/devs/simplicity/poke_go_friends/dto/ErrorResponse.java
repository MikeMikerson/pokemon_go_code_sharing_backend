package com.devs.simplicity.poke_go_friends.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response DTO.
 * Provides consistent error format across all API endpoints.
 */
@Data
@Builder
@Jacksonized
public class ErrorResponse {

    private int status;

    private String error;

    private String message;

    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private List<String> details;

    /**
     * Creates a simple error response.
     *
     * @param status  HTTP status code
     * @param error   error type
     * @param message error message
     * @param path    request path where error occurred
     * @return error response
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a validation error response with details.
     *
     * @param status  HTTP status code
     * @param error   error type
     * @param message error message
     * @param path    request path where error occurred
     * @param details list of validation error details
     * @return error response with validation details
     */
    public static ErrorResponse validationError(int status, String error, String message, String path, List<String> details) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }

    /**
     * Creates a rate limit error response.
     *
     * @param path request path where error occurred
     * @return rate limit error response
     */
    public static ErrorResponse rateLimitExceeded(String path) {
        return ErrorResponse.of(
                429,
                "Rate Limit Exceeded",
                "Too many submissions. Please try again later.",
                path
        );
    }

    /**
     * Creates a bad request error response.
     *
     * @param message error message
     * @param path    request path where error occurred
     * @return bad request error response
     */
    public static ErrorResponse badRequest(String message, String path) {
        return ErrorResponse.of(
                400,
                "Bad Request",
                message,
                path
        );
    }

    /**
     * Creates an internal server error response.
     *
     * @param path request path where error occurred
     * @return internal server error response
     */
    public static ErrorResponse internalServerError(String path) {
        return ErrorResponse.of(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                path
        );
    }
}
