package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.Goal;
import com.devs.simplicity.poke_go_friends.entity.Team;
import com.devs.simplicity.poke_go_friends.validation.ValidTeam;
import com.devs.simplicity.poke_go_friends.validation.ValidGoals;
import com.devs.simplicity.poke_go_friends.validation.NotBlankOrPattern;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request object for submitting a new Pokemon Go friend code")
public class FriendCodeSubmissionRequest {

    @NotBlank(message = "Friend code is required")
    @Pattern(regexp = "^\\d{12}$", 
             message = "Friend code must be exactly 12 digits")
    @Schema(
        description = "12-digit Pokemon Go friend code",
        example = "123456789012",
        pattern = "^\\d{12}$"
    )
    private String friendCode;

    @Size(max = 20, message = "Trainer name cannot exceed 20 characters")
    @NotBlankOrPattern(
        regexp = "^[a-zA-Z0-9]*$", 
        patternMessage = "Trainer name can only contain letters and numbers"
    )
    @Schema(
        description = "Pokemon Go trainer name",
        example = "PikachuMaster",
        maxLength = 20,
        pattern = "^[a-zA-Z0-9]*$"
    )
    private String trainerName;

    @Min(value = 1, message = "Player level must be at least 1")
    @Max(value = 50, message = "Player level cannot exceed 50")
    @Schema(
        description = "Current player level in Pokemon Go",
        example = "35",
        minimum = "1",
        maximum = "50"
    )
    private Integer playerLevel;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    @Schema(
        description = "Geographic location of the trainer",
        example = "New York, NY",
        maxLength = 200
    )
    private String location;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(
        description = "Optional description about the trainer or what they're looking for",
        example = "Looking for daily gift exchanges and raid invites!",
        maxLength = 1000
    )
    private String description;

    @ValidTeam
    @Schema(
        description = "Pokemon Go team affiliation",
        example = "MYSTIC",
        allowableValues = {"MYSTIC", "VALOR", "INSTINCT"}
    )
    private Team team;

    @ValidGoals
    @Schema(
        description = "Set of goals the trainer is interested in",
        example = "[\"GIFT_EXCHANGE\", \"RAIDS\"]"
    )
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
