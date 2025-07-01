package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.Goal;
import com.devs.simplicity.poke_go_friends.entity.Team;
import com.devs.simplicity.poke_go_friends.validation.ValidTeam;
import com.devs.simplicity.poke_go_friends.validation.ValidGoals;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * DTO for friend code update requests.
 * All fields are optional since users might want to update only specific fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendCodeUpdateRequest {

    @Size(min = 2, max = 100, message = "Trainer name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s._-]*$", 
             message = "Trainer name can only contain letters, numbers, spaces, periods, underscores, and hyphens")
    private String trainerName;

    @jakarta.validation.constraints.Min(value = 1, message = "Player level must be at least 1")
    @jakarta.validation.constraints.Max(value = 50, message = "Player level cannot exceed 50")
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
     * Check if any field is provided for update.
     */
    public boolean hasAnyUpdate() {
        return trainerName != null || playerLevel != null || location != null || 
               description != null || team != null || goals != null;
    }

    /**
     * Constructor for backwards compatibility.
     */
    public FriendCodeUpdateRequest(String trainerName, Integer playerLevel, String location, String description) {
        this.trainerName = trainerName;
        this.playerLevel = playerLevel;
        this.location = location;
        this.description = description;
    }
}
