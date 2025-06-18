package com.devs.simplicity.poke_go_friends.dto.projection;

import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projection for FriendCode feed queries (read-only, optimized for feed).
 */
public interface FriendCodeFeedProjection {
    UUID getId();
    String getFriendCode();
    String getTrainerName();
    Integer getTrainerLevel();
    Team getTeam();
    String getCountry();
    Purpose getPurpose();
    String getMessage();
    LocalDateTime getSubmittedAt();
    LocalDateTime getExpiresAt();
}
