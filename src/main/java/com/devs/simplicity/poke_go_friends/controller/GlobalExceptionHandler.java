package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.dto.ErrorResponse;
import com.devs.simplicity.poke_go_friends.exception.*;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for all REST controllers.
 * Provides consistent error responses across the application.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Validation error: {}", ex.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        String message = "Validation failed";
        String details = String.join(", ", errors);
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            message,
            details,
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle custom validation exceptions.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        log.warn("Validation exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle friend code not found exceptions.
     */
    @ExceptionHandler(FriendCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFriendCodeNotFound(
            FriendCodeNotFoundException ex, WebRequest request) {
        
        log.warn("Friend code not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle duplicate friend code exceptions.
     */
    @ExceptionHandler(DuplicateFriendCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateFriendCode(
            DuplicateFriendCodeException ex, WebRequest request) {
        
        log.warn("Duplicate friend code: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Duplicate Resource",
            ex.getMessage(),
            "The friend code has already been submitted",
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle rate limiting exceptions.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex, WebRequest request) {
        
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Rate Limit Exceeded",
            "Too many requests. Please try again later.",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    /**
     * Handle method argument type mismatch (e.g., invalid path variables).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        log.warn("Type mismatch error: {}", ex.getMessage());
        
        String message;
        
        // Check if the cause is from our custom converter with a more specific message
        Throwable rootCause = ex.getCause();
        while (rootCause != null && rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        
        if (rootCause instanceof IllegalArgumentException && 
            rootCause.getMessage() != null && 
            rootCause.getMessage().contains("Valid values are:")) {
            // Use the custom converter's detailed error message
            message = rootCause.getMessage();
        } else {
            // Use the default generic message
            message = String.format("Invalid value '%s' for parameter '%s'", 
                                   ex.getValue(), ex.getName());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Parameter",
            message,
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle malformed JSON requests.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());

        // Check for enum deserialization errors (invalid team/goals values)
        Throwable cause = ex.getCause();
        
        // Navigate through the exception chain to find our IllegalArgumentException
        while (cause != null) {
            String message = cause.getMessage();
            
            // Handle our custom IllegalArgumentException from enum fromValue methods
            if (cause instanceof IllegalArgumentException && message != null) {
                // Example message: "Invalid team value: INVALID_TEAM. Valid values are: mystic, valor, instinct"
                // or "Invalid goal value: INVALID_GOAL. Valid values are: gifts, exp, raids, all"
                if (message.contains("Invalid team value:")) {
                    String details = "team: " + message;
                    ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Error",
                        "Validation failed",
                        details,
                        request.getDescription(false).replace("uri=", "")
                    );
                    return ResponseEntity.badRequest().body(errorResponse);
                } else if (message.contains("Invalid goal value:")) {
                    String details = "goals: " + message;
                    ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Error",
                        "Validation failed",
                        details,
                        request.getDescription(false).replace("uri=", "")
                    );
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            // Check Jackson's exception messages that may contain our custom message  
            if (message != null && message.contains("problem: Invalid team value:")) {
                // Extract our custom message from Jackson's wrapper
                int problemIdx = message.indexOf("problem: ");
                if (problemIdx != -1) {
                    String customMessage = message.substring(problemIdx + 9); // Skip "problem: "
                    String details = "team: " + customMessage;
                    ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Error",
                        "Validation failed",
                        details,
                        request.getDescription(false).replace("uri=", "")
                    );
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            if (message != null && message.contains("problem: Invalid goal value:")) {
                // Extract our custom message from Jackson's wrapper
                int problemIdx = message.indexOf("problem: ");
                if (problemIdx != -1) {
                    String customMessage = message.substring(problemIdx + 9); // Skip "problem: "
                    String details = "goals: " + customMessage;
                    ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Error",
                        "Validation failed",
                        details,
                        request.getDescription(false).replace("uri=", "")
                    );
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            // Handle original Jackson enum deserialization errors (fallback)
            if (message != null && message.contains("not one of the values accepted for Enum class")) {
                // Try to extract field and allowed values from the message
                String field = null;
                String allowed = null;
                // Example: Cannot deserialize value of type `com.devs.simplicity.poke_go_friends.entity.Team` from String "INVALID_TEAM": not one of the values accepted for Enum class: [INSTINCT, MYSTIC, VALOR]
                int typeIdx = message.indexOf("type `");
                int fromIdx = message.indexOf(" from String ");
                if (typeIdx != -1 && fromIdx != -1) {
                    String typeName = message.substring(typeIdx + 6, fromIdx).replace("`", "").trim();
                    if (typeName.endsWith("Team")) field = "team";
                    if (typeName.endsWith("Goal")) field = "goals";
                }
                int allowedIdx = message.indexOf("accepted for Enum class: [");
                if (allowedIdx != -1) {
                    allowed = message.substring(allowedIdx + 26);
                    int end = allowed.indexOf("]");
                    if (end != -1) allowed = allowed.substring(0, end);
                }
                String details = (field != null && allowed != null)
                    ? String.format("%s: Invalid value. Allowed values: [%s]", field, allowed)
                    : "Invalid enum value in request body";
                ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    "Validation failed",
                    details,
                    request.getDescription(false).replace("uri=", "")
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Move to the next cause in the chain
            cause = cause.getCause();
        }

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed Request",
            "Invalid JSON format in request body",
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle general friend code exceptions.
     */
    @ExceptionHandler(FriendCodeException.class)
    public ResponseEntity<ErrorResponse> handleFriendCodeException(
            FriendCodeException ex, WebRequest request) {
        
        log.error("Friend code exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Friend Code Error",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RequestNotPermitted ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Rate Limit Exceeded",
                "Apologies, users may only submit 2 friend codes per hour. Please try again later.",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }
}