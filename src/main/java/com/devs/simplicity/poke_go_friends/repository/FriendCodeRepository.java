package com.devs.simplicity.poke_go_friends.repository;

import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FriendCode entity.
 * Provides custom queries for finding, filtering, and searching friend codes.
 * Extends JpaSpecificationExecutor for dynamic query building.
 */
@Repository
public interface FriendCodeRepository extends JpaRepository<FriendCode, Long>, JpaSpecificationExecutor<FriendCode> {

    /**
     * Find all active friend codes (not expired and isActive = true).
     * Ordered by creation date descending (most recent first).
     *
     * @param pageable Pagination information
     * @return Page of active friend codes
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findActiveFriendCodes(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Find all active friend codes without pagination.
     *
     * @param currentTime Current timestamp to check expiration
     * @return List of active friend codes
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "ORDER BY fc.createdAt DESC")
    List<FriendCode> findActiveFriendCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find active friend codes by location (case-insensitive search).
     *
     * @param location    Location to search for
     * @param currentTime Current timestamp to check expiration
     * @param pageable    Pagination information
     * @return Page of friend codes matching the location
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "AND LOWER(fc.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findActiveFriendCodesByLocation(@Param("location") String location,
                                                     @Param("currentTime") LocalDateTime currentTime,
                                                     Pageable pageable);

    /**
     * Find active friend codes by player level range.
     *
     * @param minLevel    Minimum player level (inclusive)
     * @param maxLevel    Maximum player level (inclusive)
     * @param currentTime Current timestamp to check expiration
     * @param pageable    Pagination information
     * @return Page of friend codes within the level range
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "AND fc.playerLevel BETWEEN :minLevel AND :maxLevel " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findActiveFriendCodesByLevelRange(@Param("minLevel") Integer minLevel,
                                                       @Param("maxLevel") Integer maxLevel,
                                                       @Param("currentTime") LocalDateTime currentTime,
                                                       Pageable pageable);

    /**
     * Find recent friend code submissions within a time period.
     *
     * @param since    The earliest creation time to include
     * @param pageable Pagination information
     * @return Page of recent friend codes
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.createdAt >= :since " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findRecentSubmissions(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Search friend codes by trainer name (case-insensitive).
     *
     * @param trainerName Trainer name to search for
     * @param currentTime Current timestamp to check expiration
     * @param pageable    Pagination information
     * @return Page of friend codes matching the trainer name
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "AND LOWER(fc.trainerName) LIKE LOWER(CONCAT('%', :trainerName, '%')) " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findActiveFriendCodesByTrainerName(@Param("trainerName") String trainerName,
                                                        @Param("currentTime") LocalDateTime currentTime,
                                                        Pageable pageable);

    /**
     * Search friend codes by description content (case-insensitive).
     *
     * @param description Description content to search for
     * @param currentTime Current timestamp to check expiration
     * @param pageable    Pagination information
     * @return Page of friend codes matching the description
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "AND LOWER(fc.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findActiveFriendCodesByDescription(@Param("description") String description,
                                                        @Param("currentTime") LocalDateTime currentTime,
                                                        Pageable pageable);

    /**
     * Find friend codes by user.
     *
     * @param user     The user who submitted the friend codes
     * @param pageable Pagination information
     * @return Page of friend codes submitted by the user
     */
    Page<FriendCode> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find active friend codes by user.
     *
     * @param user        The user who submitted the friend codes
     * @param currentTime Current timestamp to check expiration
     * @param pageable    Pagination information
     * @return Page of active friend codes submitted by the user
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.user = :user " +
           "AND fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findActiveFriendCodesByUser(@Param("user") User user,
                                                 @Param("currentTime") LocalDateTime currentTime,
                                                 Pageable pageable);

    /**
     * Check if a friend code already exists (for duplicate detection).
     *
     * @param friendCode The friend code to check
     * @return Optional containing the existing friend code if found
     */
    Optional<FriendCode> findByFriendCode(String friendCode);

    /**
     * Find expired friend codes that are still marked as active.
     *
     * @param currentTime Current timestamp
     * @return List of expired friend codes
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND fc.expiresAt IS NOT NULL AND fc.expiresAt <= :currentTime")
    List<FriendCode> findExpiredActiveFriendCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count active friend codes.
     *
     * @param currentTime Current timestamp to check expiration
     * @return Number of active friend codes
     */
    @Query("SELECT COUNT(fc) FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime)")
    Long countActiveFriendCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count friend codes by user.
     *
     * @param user The user
     * @return Number of friend codes submitted by the user
     */
    Long countByUser(User user);

    /**
     * Count active friend codes by user.
     *
     * @param user        The user
     * @param currentTime Current timestamp to check expiration
     * @return Number of active friend codes submitted by the user
     */
    @Query("SELECT COUNT(fc) FROM FriendCode fc WHERE fc.user = :user " +
           "AND fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime)")
    Long countActiveFriendCodesByUser(@Param("user") User user,
                                     @Param("currentTime") LocalDateTime currentTime);

    /**
     * Complex search combining multiple criteria.
     *
     * @param location       Location filter (can be null)
     * @param minLevel       Minimum player level (can be null)
     * @param maxLevel       Maximum player level (can be null)
     * @param searchText     Text to search in trainer name and description (can be null)
     * @param currentTime    Current timestamp to check expiration
     * @param pageable       Pagination information
     * @return Page of friend codes matching the criteria
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.isActive = true " +
           "AND (fc.expiresAt IS NULL OR fc.expiresAt > :currentTime) " +
           "AND (:location IS NULL OR LOWER(fc.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:minLevel IS NULL OR fc.playerLevel IS NULL OR fc.playerLevel >= :minLevel) " +
           "AND (:maxLevel IS NULL OR fc.playerLevel IS NULL OR fc.playerLevel <= :maxLevel) " +
           "AND (:searchText IS NULL OR " +
           "     LOWER(fc.trainerName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "     LOWER(fc.description) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY fc.createdAt DESC")
    Page<FriendCode> findActiveFriendCodesWithFilters(@Param("location") String location,
                                                      @Param("minLevel") Integer minLevel,
                                                      @Param("maxLevel") Integer maxLevel,
                                                      @Param("searchText") String searchText,
                                                      @Param("currentTime") LocalDateTime currentTime,
                                                      Pageable pageable);

    /**
     * Delete friend codes created before the specified timestamp.
     * This is used for cleanup of old friend codes.
     *
     * @param timestamp The cutoff timestamp - friend codes created before this will be deleted
     * @return The number of friend codes deleted
     */
    @Modifying
    @Query("DELETE FROM FriendCode fc WHERE fc.createdAt < :timestamp")
    int deleteByCreatedAtBefore(@Param("timestamp") LocalDateTime timestamp);
}
