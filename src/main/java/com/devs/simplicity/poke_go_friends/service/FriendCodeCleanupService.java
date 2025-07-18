package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service responsible for automatically cleaning up old friend codes.
 * Runs a scheduled job to delete friend codes older than 24 hours.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FriendCodeCleanupService {

    private final FriendCodeRepository friendCodeRepository;

    /**
     * Scheduled method that runs every hour to clean up old friend codes.
     * Deletes all friend codes that were created more than 24 hours ago.
     */
    @Scheduled(cron = "0 0 * * * *") // Runs at the start of every hour
    @Transactional
    public void cleanupOldFriendCodes() {
        try {
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            
            log.debug("Starting cleanup of friend codes older than: {}", twentyFourHoursAgo);
            
            int deletedCount = friendCodeRepository.deleteByCreatedAtBefore(twentyFourHoursAgo);
            
            if (deletedCount > 0) {
                log.info("Successfully deleted {} old friend codes", deletedCount);
            } else {
                log.debug("No old friend codes found to delete");
            }
            
        } catch (Exception e) {
            log.error("Error occurred during friend code cleanup: {}", e.getMessage(), e);
            // Don't rethrow the exception - we want the cleanup to continue on the next scheduled run
        }
    }
}
