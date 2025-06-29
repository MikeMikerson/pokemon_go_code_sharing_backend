package com.devs.simplicity.poke_go_friends.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a user in the Pokemon Go friend code sharing system.
 * This will be used for authentication and linking friend code submissions to users.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_username", columnList = "username", unique = true),
    @Index(name = "idx_users_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank(message = "Password is required")
    private String passwordHash;

    @Column(name = "trainer_name", length = 100)
    @Size(max = 100, message = "Trainer name cannot exceed 100 characters")
    private String trainerName;

    @Column(name = "player_level")
    @Min(value = 1, message = "Player level must be at least 1")
    @Max(value = 50, message = "Player level cannot exceed 50")
    private Integer playerLevel;

    @Column(name = "location", length = 200)
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // One-to-many relationship with FriendCode
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FriendCode> friendCodes;

    /**
     * Constructor for creating a user with essential information.
     *
     * @param username     The unique username
     * @param email        The user's email address
     * @param passwordHash The hashed password
     */
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.isActive = true;
        this.emailVerified = false;
    }

    /**
     * Constructor for creating a user with profile information.
     *
     * @param username     The unique username
     * @param email        The user's email address
     * @param passwordHash The hashed password
     * @param trainerName  The Pokemon Go trainer name
     * @param playerLevel  The player's level
     * @param location     The player's location
     */
    public User(String username, String email, String passwordHash, 
                String trainerName, Integer playerLevel, String location) {
        this(username, email, passwordHash);
        this.trainerName = trainerName;
        this.playerLevel = playerLevel;
        this.location = location;
    }

    /**
     * Deactivates the user account.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Marks the user's email as verified.
     */
    public void verifyEmail() {
        this.emailVerified = true;
    }

    /**
     * Updates the last login timestamp.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Checks if the user account is currently active and email is verified.
     *
     * @return true if the user is active and verified, false otherwise
     */
    public boolean isActiveAndVerified() {
        return Boolean.TRUE.equals(this.isActive) && Boolean.TRUE.equals(this.emailVerified);
    }
}
