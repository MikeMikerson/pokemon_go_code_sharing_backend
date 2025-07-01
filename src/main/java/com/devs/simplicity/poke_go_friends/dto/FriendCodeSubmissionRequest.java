package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.Goal;
import com.devs.simplicity.poke_go_friends.entity.Team;
import com.devs.simplicity.poke_go_friends.validation.ValidTeam;
import com.devs.simplicity.poke_go_friends.validation.ValidGoals;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * DTO for friend code submission requests.
 * Contains validation annotations for all fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendCodeSubmissionRequest {

    @NotBlank(message = "Friend code is required")
    @Pattern(regexp = "^\\d{12}$|^\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}$", 
             message = "Friend code must be exactly 12 digits (spaces and dashes will be removed)")
    private String friendCode;

    private String trainerName;

    @Min(value = 1, message = "Player level must be at least 1")
    @Max(value = 50, message = "Player level cannot exceed 50")
    private Integer playerLevel;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @ValidTeam
    private Team team;

    @ValidGoals
    private Set<Goal> goals;

    /**
     * Constructor for required fields only.
     */
    public FriendCodeSubmissionRequest(String friendCode, String trainerName) {
        this.friendCode = friendCode;
        this.trainerName = trainerName;
    }

    /**
     * Constructor for backwards compatibility.
     */
    public FriendCodeSubmissionRequest(String friendCode, String trainerName, Integer playerLevel, 
                                     String location, String description) {
        this.friendCode = friendCode;
        this.trainerName = trainerName;
        this.playerLevel = playerLevel;
        this.location = location;
        this.description = description;
    }
}
