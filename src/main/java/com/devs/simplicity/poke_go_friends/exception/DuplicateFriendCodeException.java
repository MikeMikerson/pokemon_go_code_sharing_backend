package com.devs.simplicity.poke_go_friends.exception;

/**
 * Exception thrown when a duplicate friend code is submitted.
 */
public class DuplicateFriendCodeException extends FriendCodeException {

    public DuplicateFriendCodeException(String friendCode) {
        super("Friend code already exists: " + friendCode);
    }

    public DuplicateFriendCodeException(String friendCode, String message) {
        super("Duplicate friend code '" + friendCode + "': " + message);
    }
}
