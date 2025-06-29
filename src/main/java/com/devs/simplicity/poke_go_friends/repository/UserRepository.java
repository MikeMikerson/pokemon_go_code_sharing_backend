package com.devs.simplicity.poke_go_friends.repository;

import com.devs.simplicity.poke_go_friends.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides queries for user management and authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username.
     *
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email.
     *
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email (for login).
     *
     * @param username The username to search for
     * @param email    The email to search for
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    /**
     * Check if username exists.
     *
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists.
     *
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users.
     *
     * @param pageable Pagination information
     * @return Page of active users
     */
    Page<User> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find users by trainer name (case-insensitive search).
     *
     * @param trainerName Trainer name to search for
     * @param pageable    Pagination information
     * @return Page of users matching the trainer name
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true " +
           "AND LOWER(u.trainerName) LIKE LOWER(CONCAT('%', :trainerName, '%')) " +
           "ORDER BY u.createdAt DESC")
    Page<User> findActiveUsersByTrainerName(@Param("trainerName") String trainerName, Pageable pageable);

    /**
     * Find users by location (case-insensitive search).
     *
     * @param location Location to search for
     * @param pageable Pagination information
     * @return Page of users matching the location
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true " +
           "AND LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "ORDER BY u.createdAt DESC")
    Page<User> findActiveUsersByLocation(@Param("location") String location, Pageable pageable);

    /**
     * Find users who haven't verified their email.
     *
     * @return List of unverified users
     */
    List<User> findByEmailVerifiedFalseAndIsActiveTrue();

    /**
     * Find users who haven't logged in since a specific date.
     *
     * @param date The cutoff date
     * @return List of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true " +
           "AND (u.lastLoginAt IS NULL OR u.lastLoginAt < :date)")
    List<User> findUsersInactiveSince(@Param("date") LocalDateTime date);

    /**
     * Count active users.
     *
     * @return Number of active users
     */
    Long countByIsActiveTrue();

    /**
     * Count verified users.
     *
     * @return Number of verified users
     */
    Long countByEmailVerifiedTrueAndIsActiveTrue();
}
