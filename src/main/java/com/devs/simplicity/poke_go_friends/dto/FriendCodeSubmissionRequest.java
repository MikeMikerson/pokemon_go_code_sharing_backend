package com.devs.simplicity.poke_go_friends.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for friend code submission requests.
 * Contains validation annotations for all fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendCodeSubmissionRequest {

    @NotBlank(message = "Friend code is required")
    @Pattern(regexp = "\\d{12}", message = "Friend code must be exactly 12 digits")
    private String friendCode;

    @NotBlank(message = "Trainer name is required")
    @Size(min = 2, max = 100, message = "Trainer name must be between 2 and 100 characters")
    private String trainerName;

    @Min(value = 1, message = "Player level must be at least 1")
    @Max(value = 50, message = "Player level cannot exceed 50")
    private Integer playerLevel;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    /**
     * Constructor for required fields only.
     */
    public FriendCodeSubmissionRequest(String friendCode, String trainerName) {
        this.friendCode = friendCode;
        this.trainerName = trainerName;
    }
}
