package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.entity.User;
import com.devs.simplicity.poke_go_friends.exception.DuplicateFriendCodeException;
import com.devs.simplicity.poke_go_friends.exception.FriendCodeNotFoundException;
import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import com.devs.simplicity.poke_go_friends.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Pokemon Go friend codes.
 * Provides business logic for creating, retrieving, updating, and managing friend codes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendCodeService {

    private final FriendCodeRepository friendCodeRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;

    /**
     * Creates a new friend code after validation and duplicate checking.
     *
     * @param friendCode   The 12-digit Pokemon Go friend code
     * @param trainerName  The trainer's name
     * @param playerLevel  The player's level (optional)
     * @param location     The player's location (optional)
     * @param description  Description of what they're looking for (optional)
     * @param ipAddress    The submitter's IP address for rate limiting
     * @param userId       The submitter's user ID (optional for anonymous submissions)
     * @return The created FriendCode entity
     * @throws DuplicateFriendCodeException if the friend code already exists
     */
    public FriendCode createFriendCode(String friendCode, String trainerName, Integer playerLevel,
                                      String location, String description, String ipAddress, Long userId) {
        log.info("Creating friend code: {} for trainer: {}", friendCode, trainerName);

        // Validate all input data and check rate limits
        validationService.validateFriendCodeSubmission(friendCode, trainerName, playerLevel, 
                                                       location, description, ipAddress, userId);

        // Check for duplicates
        checkForDuplicateFriendCode(friendCode);

        // Get user if provided
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new FriendCodeNotFoundException("User not found with ID: " + userId));
        }

        // Create and save the friend code
        FriendCode newFriendCode = new FriendCode(friendCode, trainerName, playerLevel, location, description);
        newFriendCode.setUser(user);

        FriendCode savedFriendCode = friendCodeRepository.save(newFriendCode);
        
        log.info("Successfully created friend code with ID: {} for trainer: {}", 
                savedFriendCode.getId(), trainerName);
        
        return savedFriendCode;
    }

    /**
     * Creates a friend code with automatic expiration.
     *
     * @param friendCode    The friend code
     * @param trainerName   The trainer name
     * @param playerLevel   The player level
     * @param location      The location
     * @param description   The description
     * @param ipAddress     The IP address
     * @param userId        The user ID
     * @param expirationDays Number of days until expiration
     * @return The created FriendCode entity
     */
    public FriendCode createFriendCodeWithExpiration(String friendCode, String trainerName, Integer playerLevel,
                                                    String location, String description, String ipAddress, 
                                                    Long userId, int expirationDays) {
        FriendCode newFriendCode = createFriendCode(friendCode, trainerName, playerLevel, 
                                                   location, description, ipAddress, userId);
        
        newFriendCode.setExpiration(LocalDateTime.now().plus(expirationDays, ChronoUnit.DAYS));
        
        return friendCodeRepository.save(newFriendCode);
    }

    /**
     * Retrieves a friend code by ID.
     *
     * @param id The friend code ID
     * @return The FriendCode entity
     * @throws FriendCodeNotFoundException if the friend code is not found
     */
    @Transactional(readOnly = true)
    public FriendCode getFriendCodeById(Long id) {
        log.debug("Retrieving friend code by ID: {}", id);
        
        return friendCodeRepository.findById(id)
                .orElseThrow(() -> new FriendCodeNotFoundException(id));
    }

    /**
     * Retrieves a friend code by the actual code value.
     *
     * @param friendCode The 12-digit friend code
     * @return The FriendCode entity
     * @throws FriendCodeNotFoundException if the friend code is not found
     */
    @Transactional(readOnly = true)
    public FriendCode getFriendCodeByValue(String friendCode) {
        log.debug("Retrieving friend code by value: {}", friendCode);
        
        validationService.validateFriendCodeFormat(friendCode);
        
        return friendCodeRepository.findByFriendCode(friendCode)
                .orElseThrow(() -> new FriendCodeNotFoundException("friendCode", friendCode));
    }

    /**
     * Gets a paginated list of all active friend codes.
     *
     * @param pageable Pagination information
     * @return Page of active friend codes
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> getActiveFriendCodes(Pageable pageable) {
        log.debug("Retrieving active friend codes with pagination: {}", pageable);
        
        return friendCodeRepository.findActiveFriendCodes(LocalDateTime.now(), pageable);
    }

    /**
     * Filters friend codes by location.
     *
     * @param location The location to filter by
     * @param pageable Pagination information
     * @return Page of friend codes matching the location
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> getFriendCodesByLocation(String location, Pageable pageable) {
        log.debug("Filtering friend codes by location: {}", location);
        
        if (!StringUtils.hasText(location)) {
            return getActiveFriendCodes(pageable);
        }
        
        return friendCodeRepository.findActiveFriendCodesByLocation(location, LocalDateTime.now(), pageable);
    }

    /**
     * Filters friend codes by player level range.
     *
     * @param minLevel Minimum player level (inclusive)
     * @param maxLevel Maximum player level (inclusive)
     * @param pageable Pagination information
     * @return Page of friend codes within the level range
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> getFriendCodesByLevelRange(Integer minLevel, Integer maxLevel, Pageable pageable) {
        log.debug("Filtering friend codes by level range: {} - {}", minLevel, maxLevel);
        
        if (minLevel != null) {
            validationService.validatePlayerLevel(minLevel);
        }
        if (maxLevel != null) {
            validationService.validatePlayerLevel(maxLevel);
        }
        
        if (minLevel == null && maxLevel == null) {
            return getActiveFriendCodes(pageable);
        }
        
        // Set default values if one is missing
        int min = minLevel != null ? minLevel : 1;
        int max = maxLevel != null ? maxLevel : 50;
        
        return friendCodeRepository.findActiveFriendCodesByLevelRange(min, max, LocalDateTime.now(), pageable);
    }

    /**
     * Searches friend codes by trainer name.
     *
     * @param trainerName The trainer name to search for
     * @param pageable    Pagination information
     * @return Page of friend codes matching the trainer name
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> searchByTrainerName(String trainerName, Pageable pageable) {
        log.debug("Searching friend codes by trainer name: {}", trainerName);
        
        if (!StringUtils.hasText(trainerName)) {
            return getActiveFriendCodes(pageable);
        }
        
        return friendCodeRepository.findActiveFriendCodesByTrainerName(trainerName, LocalDateTime.now(), pageable);
    }

    /**
     * Searches friend codes by description content.
     *
     * @param description The description text to search for
     * @param pageable    Pagination information
     * @return Page of friend codes matching the description
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> searchByDescription(String description, Pageable pageable) {
        log.debug("Searching friend codes by description: {}", description);
        
        if (!StringUtils.hasText(description)) {
            return getActiveFriendCodes(pageable);
        }
        
        return friendCodeRepository.findActiveFriendCodesByDescription(description, LocalDateTime.now(), pageable);
    }

    /**
     * Advanced search with multiple filters.
     *
     * @param location    Location filter (optional)
     * @param minLevel    Minimum player level (optional)
     * @param maxLevel    Maximum player level (optional)
     * @param searchText  Text to search in trainer name and description (optional)
     * @param pageable    Pagination information
     * @return Page of friend codes matching the filters
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> searchWithFilters(String location, Integer minLevel, Integer maxLevel,
                                             String searchText, Pageable pageable) {
        log.debug("Searching friend codes with filters - Location: {}, Level: {}-{}, Text: {}", 
                 location, minLevel, maxLevel, searchText);
        
        // Validate level inputs if provided
        if (minLevel != null) {
            validationService.validatePlayerLevel(minLevel);
        }
        if (maxLevel != null) {
            validationService.validatePlayerLevel(maxLevel);
        }
        
        return friendCodeRepository.findActiveFriendCodesWithFilters(
                location, minLevel, maxLevel, searchText, LocalDateTime.now(), pageable);
    }

    /**
     * Gets friend codes submitted by a specific user.
     *
     * @param userId   The user ID
     * @param pageable Pagination information
     * @return Page of friend codes submitted by the user
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> getFriendCodesByUser(Long userId, Pageable pageable) {
        log.debug("Retrieving friend codes for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FriendCodeNotFoundException("User not found with ID: " + userId));
        
        return friendCodeRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Gets active friend codes submitted by a specific user.
     *
     * @param userId   The user ID
     * @param pageable Pagination information
     * @return Page of active friend codes submitted by the user
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> getActiveFriendCodesByUser(Long userId, Pageable pageable) {
        log.debug("Retrieving active friend codes for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FriendCodeNotFoundException("User not found with ID: " + userId));
        
        return friendCodeRepository.findActiveFriendCodesByUser(user, LocalDateTime.now(), pageable);
    }

    /**
     * Gets recent friend code submissions.
     *
     * @param hours    Number of hours to look back
     * @param pageable Pagination information
     * @return Page of recent friend codes
     */
    @Transactional(readOnly = true)
    public Page<FriendCode> getRecentSubmissions(int hours, Pageable pageable) {
        log.debug("Retrieving friend codes from last {} hours", hours);
        
        LocalDateTime since = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        return friendCodeRepository.findRecentSubmissions(since, pageable);
    }

    /**
     * Updates a friend code (only allowed by the owner).
     *
     * @param id          The friend code ID
     * @param trainerName New trainer name (optional)
     * @param playerLevel New player level (optional)
     * @param location    New location (optional)
     * @param description New description (optional)
     * @param userId      The user making the update (must be the owner)
     * @return The updated FriendCode entity
     * @throws FriendCodeNotFoundException if the friend code is not found
     */
    public FriendCode updateFriendCode(Long id, String trainerName, Integer playerLevel,
                                      String location, String description, Long userId) {
        log.info("Updating friend code: {} by user: {}", id, userId);
        
        FriendCode friendCode = getFriendCodeById(id);
        
        // Check ownership (either the owner or admin)
        if (userId != null && friendCode.getUser() != null && 
            !friendCode.getUser().getId().equals(userId)) {
            throw new FriendCodeNotFoundException("Friend code not found or access denied");
        }
        
        // Validate new values if provided
        if (StringUtils.hasText(trainerName)) {
            validationService.validateTrainerName(trainerName);
            friendCode.setTrainerName(trainerName);
        }
        
        if (playerLevel != null) {
            validationService.validatePlayerLevel(playerLevel);
            friendCode.setPlayerLevel(playerLevel);
        }
        
        if (location != null) {
            validationService.validateLocation(location);
            friendCode.setLocation(location);
        }
        
        if (description != null) {
            validationService.validateDescription(description);
            friendCode.setDescription(description);
        }
        
        FriendCode updatedFriendCode = friendCodeRepository.save(friendCode);
        
        log.info("Successfully updated friend code: {}", id);
        return updatedFriendCode;
    }

    /**
     * Marks a friend code as inactive (soft delete).
     *
     * @param id     The friend code ID
     * @param userId The user requesting the deactivation (must be the owner)
     * @throws FriendCodeNotFoundException if the friend code is not found
     */
    public void deactivateFriendCode(Long id, Long userId) {
        log.info("Deactivating friend code: {} by user: {}", id, userId);
        
        FriendCode friendCode = getFriendCodeById(id);
        
        // Check ownership (either the owner or admin)
        if (userId != null && friendCode.getUser() != null && 
            !friendCode.getUser().getId().equals(userId)) {
            throw new FriendCodeNotFoundException("Friend code not found or access denied");
        }
        
        friendCode.deactivate();
        friendCodeRepository.save(friendCode);
        
        log.info("Successfully deactivated friend code: {}", id);
    }

    /**
     * Sets an expiration date for a friend code.
     *
     * @param id         The friend code ID
     * @param expiresAt  The expiration date
     * @param userId     The user setting the expiration (must be the owner)
     * @return The updated FriendCode entity
     */
    public FriendCode setFriendCodeExpiration(Long id, LocalDateTime expiresAt, Long userId) {
        log.info("Setting expiration for friend code: {} to: {}", id, expiresAt);
        
        FriendCode friendCode = getFriendCodeById(id);
        
        // Check ownership
        if (userId != null && friendCode.getUser() != null && 
            !friendCode.getUser().getId().equals(userId)) {
            throw new FriendCodeNotFoundException("Friend code not found or access denied");
        }
        
        friendCode.setExpiration(expiresAt);
        FriendCode updatedFriendCode = friendCodeRepository.save(friendCode);
        
        log.info("Successfully set expiration for friend code: {}", id);
        return updatedFriendCode;
    }

    /**
     * Checks for duplicate friend codes.
     *
     * @param friendCode The friend code to check
     * @throws DuplicateFriendCodeException if a duplicate is found
     */
    private void checkForDuplicateFriendCode(String friendCode) {
        Optional<FriendCode> existing = friendCodeRepository.findByFriendCode(friendCode);
        
        if (existing.isPresent()) {
            FriendCode existingCode = existing.get();
            
            // If the existing code is active, it's a duplicate
            if (existingCode.isCurrentlyActive()) {
                throw new DuplicateFriendCodeException(friendCode);
            }
            
            // If the existing code is inactive, we could allow resubmission
            // but for now, we'll still consider it a duplicate
            throw new DuplicateFriendCodeException(friendCode, 
                    "Friend code was previously submitted but is now inactive");
        }
    }

    /**
     * Cleanup expired friend codes (mark as inactive).
     * This method should be called periodically by a scheduled task.
     *
     * @return Number of friend codes that were deactivated
     */
    public int cleanupExpiredFriendCodes() {
        log.info("Starting cleanup of expired friend codes");
        
        List<FriendCode> expiredCodes = friendCodeRepository.findExpiredActiveFriendCodes(LocalDateTime.now());
        
        int count = 0;
        for (FriendCode friendCode : expiredCodes) {
            friendCode.deactivate();
            friendCodeRepository.save(friendCode);
            count++;
        }
        
        log.info("Cleanup completed. Deactivated {} expired friend codes", count);
        return count;
    }

    /**
     * Gets statistics about friend codes.
     *
     * @return Statistics object with various counts
     */
    @Transactional(readOnly = true)
    public FriendCodeStats getStatistics() {
        log.debug("Generating friend code statistics");
        
        LocalDateTime now = LocalDateTime.now();
        
        long totalActive = friendCodeRepository.countActiveFriendCodes(now);
        long totalAll = friendCodeRepository.count();
        
        return new FriendCodeStats(totalActive, totalAll);
    }

    /**
     * Statistics data class.
     */
    public static class FriendCodeStats {
        private final long activeFriendCodes;
        private final long totalFriendCodes;
        
        public FriendCodeStats(long activeFriendCodes, long totalFriendCodes) {
            this.activeFriendCodes = activeFriendCodes;
            this.totalFriendCodes = totalFriendCodes;
        }
        
        public long getActiveFriendCodes() {
            return activeFriendCodes;
        }
        
        public long getTotalFriendCodes() {
            return totalFriendCodes;
        }
        
        public long getInactiveFriendCodes() {
            return totalFriendCodes - activeFriendCodes;
        }
    }
}
