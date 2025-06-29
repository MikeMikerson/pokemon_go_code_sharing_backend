package com.devs.simplicity.poke_go_friends.exception;

/**
 * Base exception class for Friend Code related errors.
 */
public class FriendCodeException extends RuntimeException {

    public FriendCodeException(String message) {
        super(message);
    }

    public FriendCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
