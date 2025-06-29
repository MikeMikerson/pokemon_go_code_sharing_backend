package com.devs.simplicity.poke_go_friends.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

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
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;

    /**
     * Constructor for basic friend code info.
     */
    public FriendCodeResponse(Long id, String friendCode, String trainerName) {
        this.id = id;
        this.friendCode = friendCode;
        this.trainerName = trainerName;
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
            entity.getIsActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getExpiresAt()
        );
    }
}
