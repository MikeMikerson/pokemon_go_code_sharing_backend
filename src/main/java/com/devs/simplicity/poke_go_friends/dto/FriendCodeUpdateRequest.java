package com.devs.simplicity.poke_go_friends.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for friend code update requests.
 * All fields are optional since users might want to update only specific fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendCodeUpdateRequest {

    @Size(min = 2, max = 100, message = "Trainer name must be between 2 and 100 characters")
    private String trainerName;

    @jakarta.validation.constraints.Min(value = 1, message = "Player level must be at least 1")
    @jakarta.validation.constraints.Max(value = 50, message = "Player level cannot exceed 50")
    private Integer playerLevel;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    /**
     * Check if any field is provided for update.
     */
    public boolean hasAnyUpdate() {
        return trainerName != null || playerLevel != null || location != null || description != null;
    }
}
