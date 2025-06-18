package com.devs.simplicity.poke_go_friends.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

/**
 * Response DTO for checking if a user can submit a friend code.
 * Contains submission eligibility status and next allowed submission time.
 */
@Data
@Builder
@Jacksonized
public class CanSubmitResponse {

    private boolean canSubmit;
    private Instant nextSubmissionTime;
}
