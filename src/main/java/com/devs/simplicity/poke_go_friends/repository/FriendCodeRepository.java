package com.devs.simplicity.poke_go_friends.repository;

import com.devs.simplicity.poke_go_friends.model.FriendCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing FriendCode entities.
 * Provides custom queries for common operations like finding active codes
 * and cleaning up expired entries.
 */
@Repository
public interface FriendCodeRepository extends JpaRepository<FriendCode, UUID> {

    /**
     * Finds all active (non-expired) friend codes, ordered by submission date descending.
     *
     * @param currentTime the current timestamp to compare against expiration
     * @param pageable    pagination information
     * @return page of active friend codes
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.expiresAt > :currentTime ORDER BY fc.submittedAt DESC")
    Page<FriendCode> findActiveFriendCodes(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Finds the most recent submission by a user fingerprint.
     *
     * @param userFingerprint the user's fingerprint
     * @param pageable pagination with size 1 to get the most recent entry
     * @return the most recent friend code submission by this user, if any
     */
    @Query("SELECT fc FROM FriendCode fc WHERE fc.userFingerprint = :userFingerprint " +
           "ORDER BY fc.submittedAt DESC")
    List<FriendCode> findMostRecentByUserFingerprintOrderBySubmittedAtDesc(
            @Param("userFingerprint") String userFingerprint, Pageable pageable);

    /**
     * Counts the number of submissions by a user within a time period.
     *
     * @param userFingerprint the user's fingerprint
     * @param since           the start time for counting submissions
     * @return the number of submissions since the given time
     */
    @Query("SELECT COUNT(fc) FROM FriendCode fc WHERE fc.userFingerprint = :userFingerprint " +
           "AND fc.submittedAt >= :since")
    long countSubmissionsByUserSince(@Param("userFingerprint") String userFingerprint,
                                     @Param("since") LocalDateTime since);

    /**
     * Deletes all expired friend codes.
     * This method should be called periodically to clean up old data.
     *
     * @param currentTime the current timestamp
     * @return the number of deleted records
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM FriendCode fc WHERE fc.expiresAt <= :currentTime")
    int deleteExpiredFriendCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Counts the total number of active friend codes.
     *
     * @param currentTime the current timestamp to compare against expiration
     * @return the count of active friend codes
     */
    @Query("SELECT COUNT(fc) FROM FriendCode fc WHERE fc.expiresAt > :currentTime")
    long countActiveFriendCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find all friend codes that haven't expired, ordered by submission date (newest first).
     * This method supports pagination to handle large datasets efficiently.
     *
     * @param currentTime the current timestamp to compare against expiration
     * @param pageable pagination information
     * @return list of non-expired friend codes
     */
    List<FriendCode> findAllByExpiresAtAfterOrderBySubmittedAtDesc(LocalDateTime currentTime, Pageable pageable);

    /**
     * Find friend codes submitted by a specific user (identified by fingerprint) after a given time.
     * Used for rate limiting to check if a user has submitted recently.
     *
     * @param userFingerprint the unique fingerprint of the user
     * @param submittedAfter the earliest submission time to consider
     * @return list of friend codes matching the criteria
     */
    List<FriendCode> findByUserFingerprintAndSubmittedAtAfter(String userFingerprint, LocalDateTime submittedAfter);

    /**
     * Delete all friend codes that have expired before the given time.
     * This is a cleanup operation to remove old entries.
     *
     * @param cutoffTime the timestamp before which all friend codes should be deleted
     * @return the number of deleted records
     */
    @Modifying
    @Query("DELETE FROM FriendCode f WHERE f.expiresAt < :cutoffTime")
    int deleteByExpiresAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count the number of active (non-expired) friend codes.
     *
     * @param currentTime the current timestamp to compare against expiration
     * @return count of active friend codes
     */
    long countByExpiresAtAfter(LocalDateTime currentTime);
    
    /**
     * Convenience method to find the most recent submission by a user fingerprint.
     * Returns a single result or null if none found.
     *
     * @param userFingerprint the user's fingerprint
     * @return the most recent friend code submission by this user, or null if none found
     */
    default FriendCode findMostRecentByUserFingerprint(String userFingerprint) {
        List<FriendCode> results = findMostRecentByUserFingerprintOrderBySubmittedAtDesc(
                userFingerprint, PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Retrieves a page of active (non-expired) friend codes as a projection for feed optimization.
     *
     * @param currentTime the current timestamp to compare against expiration
     * @param pageable    pagination information
     * @return page of FriendCodeFeedProjection
     */
    @Query("SELECT fc.id as id, fc.friendCode as friendCode, fc.trainerName as trainerName, fc.trainerLevel as trainerLevel, fc.team as team, fc.country as country, fc.purpose as purpose, fc.message as message, fc.submittedAt as submittedAt, fc.expiresAt as expiresAt FROM FriendCode fc WHERE fc.expiresAt > :currentTime ORDER BY fc.submittedAt DESC")
    Page<com.devs.simplicity.poke_go_friends.dto.projection.FriendCodeFeedProjection> findActiveFriendCodesProjected(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);
}
