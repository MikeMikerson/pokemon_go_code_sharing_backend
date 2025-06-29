package com.devs.simplicity.poke_go_friends.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO for standardized error responses.
 * Provides consistent error format across all API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private String path;

    /**
     * Constructor for basic error response.
     */
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with path information.
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message);
        this.path = path;
    }

    /**
     * Constructor with full details.
     */
    public ErrorResponse(int status, String error, String message, String details, String path) {
        this(status, error, message, path);
        this.details = details;
    }

    /**
     * Factory methods for common error types.
     */
    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse(400, "Bad Request", message);
    }

    public static ErrorResponse notFound(String message) {
        return new ErrorResponse(404, "Not Found", message);
    }

    public static ErrorResponse conflict(String message) {
        return new ErrorResponse(409, "Conflict", message);
    }

    public static ErrorResponse tooManyRequests(String message) {
        return new ErrorResponse(429, "Too Many Requests", message);
    }

    public static ErrorResponse internalServerError(String message) {
        return new ErrorResponse(500, "Internal Server Error", message);
    }
}
