package com.devs.simplicity.poke_go_friends.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
