package com.devs.simplicity.poke_go_friends.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * Response DTO for friend code submission.
 * Contains the result of a submission attempt and rate limiting information.
 */
@Data
@Builder
@Jacksonized
public class SubmissionResponse {

    private boolean success;

    private String message;

    private LocalDateTime nextSubmissionAllowed;

    private FriendCodeResponse friendCode;

    /**
     * Creates a successful submission response.
     *
     * @param friendCode             the created friend code
     * @param nextSubmissionAllowed  when the user can submit again
     * @return a successful submission response
     */
    public static SubmissionResponse success(FriendCodeResponse friendCode, LocalDateTime nextSubmissionAllowed) {
        return SubmissionResponse.builder()
                .success(true)
                .message("Friend code submitted successfully")
                .friendCode(friendCode)
                .nextSubmissionAllowed(nextSubmissionAllowed)
                .build();
    }

    /**
     * Creates a rate-limited submission response.
     *
     * @param nextSubmissionAllowed when the user can submit again
     * @return a rate-limited submission response
     */
    public static SubmissionResponse rateLimited(LocalDateTime nextSubmissionAllowed) {
        return SubmissionResponse.builder()
                .success(false)
                .message("Rate limit exceeded. Please try again later.")
                .nextSubmissionAllowed(nextSubmissionAllowed)
                .build();
    }

    /**
     * Creates a validation error submission response.
     *
     * @param message the validation error message
     * @return a validation error submission response
     */
    public static SubmissionResponse validationError(String message) {
        return SubmissionResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
