package com.devs.simplicity.poke_go_friends.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Pokémon Go friend code submission.
 * Contains all information needed for trainers to connect with each other.
 */
@Entity
@Table(name = "friend_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Friend code is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Friend code must be exactly 12 digits")
    @Column(name = "friend_code", nullable = false, length = 12)
    private String friendCode;

    @Size(max = 50, message = "Trainer name cannot exceed 50 characters")
    @Column(name = "trainer_name", length = 50)
    private String trainerName;

    @Min(value = 1, message = "Trainer level must be at least 1")
    @Max(value = 50, message = "Trainer level cannot exceed 50")
    @Column(name = "trainer_level")
    private Integer trainerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "team", length = 20)
    private Team team;

    @Size(max = 50, message = "Country name cannot exceed 50 characters")
    @Column(name = "country", length = 50)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 20)
    private Purpose purpose;

    @Size(max = 100, message = "Message cannot exceed 100 characters")
    @Column(name = "message", length = 100)
    private String message;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @NotBlank(message = "User fingerprint is required")
    @Size(max = 255, message = "User fingerprint cannot exceed 255 characters")
    @Column(name = "user_fingerprint", nullable = false)
    private String userFingerprint;

    /**
     * Sets the submission timestamp before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Default expiration is 48 hours from submission
            expiresAt = submittedAt.plusHours(48);
        }
    }

    /**
     * Checks if this friend code has expired.
     *
     * @return true if the friend code has expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if this friend code is still active.
     *
     * @return true if the friend code is still active, false otherwise
     */
    public boolean isActive() {
        return !isExpired();
    }
}
