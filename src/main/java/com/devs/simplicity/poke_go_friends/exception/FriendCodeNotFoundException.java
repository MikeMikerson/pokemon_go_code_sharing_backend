package com.devs.simplicity.poke_go_friends.exception;

/**
 * Exception thrown when a friend code is not found.
 */
public class FriendCodeNotFoundException extends FriendCodeException {

    public FriendCodeNotFoundException(String message) {
        super(message);
    }

    public FriendCodeNotFoundException(Long id) {
        super("Friend code not found with ID: " + id);
    }

    public FriendCodeNotFoundException(String fieldName, String value) {
        super("Friend code not found with " + fieldName + ": " + value);
    }
}
