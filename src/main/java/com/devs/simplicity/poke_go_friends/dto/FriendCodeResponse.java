package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.Goal;
import com.devs.simplicity.poke_go_friends.entity.Team;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

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
public class FriendCodeResponse {

    private Long id;
    private String friendCode;
    private String trainerName;
    private Integer playerLevel;
    private String location;
    private String description;
    private Team team;
    private Set<Goal> goals;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
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
