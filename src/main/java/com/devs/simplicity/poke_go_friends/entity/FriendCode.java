package com.devs.simplicity.poke_go_friends.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * Entity representing a Pokemon Go friend code submission.
 * Contains all necessary information for friend code sharing.
 */
@Entity
@Table(name = "friend_codes", indexes = {
    @Index(name = "idx_friend_codes_active", columnList = "isActive"),
    @Index(name = "idx_friend_codes_created", columnList = "createdAt"),
    @Index(name = "idx_friend_codes_location", columnList = "location")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FriendCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "friend_code", unique = true, nullable = false, length = 12)
    @NotBlank(message = "Friend code is required")
    @Pattern(regexp = "\\d{12}", message = "Friend code must be exactly 12 digits")
    private String friendCode;

    @Column(name = "trainer_name", nullable = true, length = 20)
    @Size(max = 20, message = "Trainer name cannot exceed 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", 
             message = "Trainer name can only contain letters and numbers")
    private String trainerName;

    @Column(name = "player_level")
    @Min(value = 1, message = "Player level must be at least 1")
    @Max(value = 50, message = "Player level cannot exceed 50")
    private Integer playerLevel;

    @Column(name = "location", length = 200)
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "team", length = 20)
    private Team team;

    @ElementCollection(targetClass = Goal.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "friend_code_goals", joinColumns = @JoinColumn(name = "friend_code_id"))
    @Column(name = "goal")
    private Set<Goal> goals = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Many-to-one relationship with User (optional for now, can be null for anonymous submissions)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Constructor for creating a friend code with required fields.
     *
     * @param friendCode   The 12-digit Pokemon Go friend code
     * @param trainerName  The Pokemon Go trainer name
     */
    public FriendCode(String friendCode, String trainerName) {
        this.friendCode = friendCode;
        this.trainerName = trainerName;
        this.isActive = true;
    }

    /**
     * Constructor for creating a friend code with all optional fields.
     *
     * @param friendCode   The 12-digit Pokemon Go friend code
     * @param trainerName  The Pokemon Go trainer name
     * @param playerLevel  The player's level (optional)
     * @param location     The player's location (optional)
     * @param description  Description of what they're looking for (optional)
     */
    public FriendCode(String friendCode, String trainerName, Integer playerLevel, 
                     String location, String description) {
        this(friendCode, trainerName);
        this.playerLevel = playerLevel;
        this.location = location;
        this.description = description;
    }

    /**
     * Constructor for creating a friend code with all fields including team and goals.
     *
     * @param friendCode   The 12-digit Pokemon Go friend code
     * @param trainerName  The Pokemon Go trainer name
     * @param playerLevel  The player's level (optional)
     * @param location     The player's location (optional)
     * @param description  Description of what they're looking for (optional)
     * @param team         The Pokemon Go team (optional)
     * @param goals        The friendship goals (optional)
     */
    public FriendCode(String friendCode, String trainerName, Integer playerLevel, 
                     String location, String description, Team team, Set<Goal> goals) {
        this(friendCode, trainerName, playerLevel, location, description);
        this.team = team;
        this.goals = goals != null ? new HashSet<>(goals) : new HashSet<>();
    }

    /**
     * Checks if the friend code is currently active and not expired.
     *
     * @return true if active and not expired, false otherwise
     */
    public boolean isCurrentlyActive() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        
        if (expiresAt != null) {
            return LocalDateTime.now().isBefore(expiresAt);
        }
        
        return true;
    }

    /**
     * Deactivates the friend code.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Sets an expiration date for the friend code.
     *
     * @param expiresAt The expiration date
     */
    public void setExpiration(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Adds a goal to the friend code.
     *
     * @param goal The goal to add
     */
    public void addGoal(Goal goal) {
        if (this.goals == null) {
            this.goals = new HashSet<>();
        }
        this.goals.add(goal);
    }

    /**
     * Removes a goal from the friend code.
     *
     * @param goal The goal to remove
     */
    public void removeGoal(Goal goal) {
        if (this.goals != null) {
            this.goals.remove(goal);
        }
    }

    /**
     * Sets the goals for the friend code.
     *
     * @param goals The set of goals
     */
    public void setGoals(Set<Goal> goals) {
        this.goals = goals != null ? new HashSet<>(goals) : new HashSet<>();
    }
}
