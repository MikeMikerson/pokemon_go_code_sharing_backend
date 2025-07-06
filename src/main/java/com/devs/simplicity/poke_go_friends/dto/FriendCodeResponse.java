package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.Goal;
import com.devs.simplicity.poke_go_friends.entity.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for friend code response data.
 * Contains all public fields that should be exposed via API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response object containing friend code information")
public class FriendCodeResponse {

    @Schema(description = "Unique identifier for the friend code", example = "123")
    private Long id;
    
    @Schema(description = "Pokemon Go friend code", example = "123456789012")
    private String friendCode;
    
    @Schema(description = "Pokemon Go trainer name", example = "PikachuMaster", maxLength = 20)
    private String trainerName;
    
    @Schema(description = "Current player level", example = "35", minimum = "1", maximum = "50")
    private Integer playerLevel;
    
    @Schema(description = "Geographic location", example = "New York, NY")
    private String location;
    
    @Schema(description = "Description about the trainer", example = "Looking for daily gift exchanges!")
    private String description;
    
    @Schema(description = "Pokemon Go team", example = "MYSTIC")
    private Team team;
    
    @Schema(description = "Set of goals the trainer is interested in")
    private Set<Goal> goals;
    
    @Schema(description = "Whether the friend code is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "When the friend code was created", example = "2025-07-03T04:16:18.278Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    
    @Schema(description = "When the friend code was last updated", example = "2025-07-03T04:16:18.278Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;
    
    @Schema(description = "When the friend code expires (optional)", example = "2025-08-03T04:16:18.278Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime expiresAt;


    /**
     * Constructor for backwards compatibility.
     */
    public FriendCodeResponse(Long id, String friendCode, String trainerName, Integer playerLevel,
                            String location, String description, Boolean isActive, 
                            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime expiresAt) {
        this.id = id;
        this.friendCode = friendCode;
        this.trainerName = trainerName;
        this.playerLevel = playerLevel;
        this.location = location;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Factory method to create response from entity.
     * Note: In a real application, you might use MapStruct or similar for mapping.
     */
    public static FriendCodeResponse fromEntity(com.devs.simplicity.poke_go_friends.entity.FriendCode entity) {
        return new FriendCodeResponse(
            entity.getId(),
            entity.getFriendCode(),
            entity.getTrainerName(),
            entity.getPlayerLevel(),
            entity.getLocation(),
            entity.getDescription(),
            entity.getTeam(),
            entity.getGoals(),
            entity.getIsActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getExpiresAt()
        );
    }
}
