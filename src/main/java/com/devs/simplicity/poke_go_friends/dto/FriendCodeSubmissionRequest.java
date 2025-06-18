package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import com.devs.simplicity.poke_go_friends.validation.CountryCode;
import com.devs.simplicity.poke_go_friends.validation.NoHtml;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Request DTO for submitting a new friend code.
 * Contains all the information that a user can provide when sharing their friend code.
 */
@Data
@Builder
@Jacksonized
public class FriendCodeSubmissionRequest {

    @NotBlank(message = "Friend code is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Friend code must be exactly 12 digits")
    private String friendCode;

    @Size(max = 50, message = "Trainer name cannot exceed 50 characters")
    @NoHtml(message = "Trainer name cannot contain HTML or script content")
    private String trainerName;

    @Min(value = 1, message = "Trainer level must be at least 1")
    @Max(value = 50, message = "Trainer level cannot exceed 50")
    private Integer trainerLevel;

    private Team team;

    @CountryCode(message = "Invalid country code")
    private String country;

    private Purpose purpose;

    @Size(max = 100, message = "Message cannot exceed 100 characters")
    @NoHtml(message = "Message cannot contain HTML or script content")
    private String message;
}
