package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for cleaning up expired friend codes from the database.
 * Runs scheduled tasks to maintain database hygiene by removing expired entries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final FriendCodeRepository friendCodeRepository;

    /**
     * Deletes all friend codes that have expired.
     * This method can be called manually or by the scheduled task.
     * 
     * @return the number of friend codes that were deleted
     */
    @Transactional
    public int cleanupExpiredFriendCodes() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("Starting cleanup of friend codes expired before: {}", now);
        
        try {
            int deletedCount = friendCodeRepository.deleteByExpiresAtBefore(now);
            
            if (deletedCount > 0) {
                log.info("Cleanup completed: {} expired friend codes deleted", deletedCount);
            } else {
                log.debug("Cleanup completed: no expired friend codes found");
            }
            
            return deletedCount;
        } catch (Exception e) {
            log.error("Error during friend code cleanup", e);
            throw e;
        }
    }

    /**
     * Scheduled task that runs to clean up expired friend codes.
     * Uses configurable cron expression from application properties.
     * Default: every hour at minute 0 (0 0 * * * *)
     */
    @Scheduled(cron = "${app.cleanup.interval-cron:0 0 * * * *}")
    @Transactional
    public void scheduledCleanup() {
        log.debug("Starting scheduled cleanup of expired friend codes");
        
        try {
            int deletedCount = cleanupExpiredFriendCodes();
            log.info("Scheduled cleanup completed: {} expired friend codes removed", deletedCount);
        } catch (Exception e) {
            log.error("Scheduled cleanup failed", e);
            // Don't rethrow - we don't want to break the scheduler
        }
    }
}
