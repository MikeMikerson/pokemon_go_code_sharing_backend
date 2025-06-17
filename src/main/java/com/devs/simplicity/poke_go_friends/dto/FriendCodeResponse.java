package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for friend code data.
 * Contains the information about a friend code that is returned to clients.
 */
@Data
@Builder
@Jacksonized
public class FriendCodeResponse {

    private UUID id;

    private String friendCode;

    private String trainerName;

    private Integer trainerLevel;

    private Team team;

    private String country;

    private Purpose purpose;

    private String message;

    private LocalDateTime submittedAt;

    private LocalDateTime expiresAt;
}
