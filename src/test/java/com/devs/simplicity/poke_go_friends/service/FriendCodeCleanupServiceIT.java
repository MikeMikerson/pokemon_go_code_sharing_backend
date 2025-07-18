package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FriendCodeCleanupService.
 * Tests the end-to-end cleanup functionality with real database operations.
 */
@DataJpaTest
@Import(FriendCodeCleanupService.class)
@ActiveProfiles("test")
@DisplayName("FriendCodeCleanupService Integration Tests")
class FriendCodeCleanupServiceIT {

    @Autowired
    private FriendCodeCleanupService friendCodeCleanupService;

    @Autowired
    private FriendCodeRepository friendCodeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        friendCodeRepository.deleteAll();
    }

    @Test
    @DisplayName("Should delete only friend codes older than 24 hours")
    void shouldDeleteOnlyOldFriendCodes() {
        // Given - Create friend codes with different timestamps
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFiveHoursAgo = now.minus(25, ChronoUnit.HOURS);
        LocalDateTime twentyThreeHoursAgo = now.minus(23, ChronoUnit.HOURS);

        // Create old friend codes (should be deleted)
        FriendCode oldFriendCode1 = new FriendCode("111111111111", "OldTrainer1");
        FriendCode oldFriendCode2 = new FriendCode("222222222222", "OldTrainer2");

        // Create recent friend codes (should remain)
        FriendCode recentFriendCode1 = new FriendCode("333333333333", "RecentTrainer1");
        FriendCode recentFriendCode2 = new FriendCode("444444444444", "RecentTrainer2");

        // Save all friend codes first (this will set the automatic timestamps)
        oldFriendCode1 = entityManager.persistAndFlush(oldFriendCode1);
        oldFriendCode2 = entityManager.persistAndFlush(oldFriendCode2);
        recentFriendCode1 = entityManager.persistAndFlush(recentFriendCode1);
        recentFriendCode2 = entityManager.persistAndFlush(recentFriendCode2);

        // Update the old friend codes to have old timestamps using native SQL
        entityManager.getEntityManager().createNativeQuery(
            "UPDATE friend_codes SET created_at = ? WHERE id = ?")
            .setParameter(1, twentyFiveHoursAgo)
            .setParameter(2, oldFriendCode1.getId())
            .executeUpdate();

        entityManager.getEntityManager().createNativeQuery(
            "UPDATE friend_codes SET created_at = ? WHERE id = ?")
            .setParameter(1, twentyFiveHoursAgo)
            .setParameter(2, oldFriendCode2.getId())
            .executeUpdate();

        // Update one recent friend code to have a timestamp just under 24 hours ago
        entityManager.getEntityManager().createNativeQuery(
            "UPDATE friend_codes SET created_at = ? WHERE id = ?")
            .setParameter(1, twentyThreeHoursAgo)
            .setParameter(2, recentFriendCode1.getId())
            .executeUpdate();

        entityManager.flush();
        entityManager.clear(); // Clear the persistence context

        // Verify initial state
        assertEquals(4, friendCodeRepository.count());

        // When - trigger cleanup
        friendCodeCleanupService.cleanupOldFriendCodes();

        // Then - verify only old friend codes were deleted
        List<FriendCode> remainingFriendCodes = friendCodeRepository.findAll();
        assertEquals(2, remainingFriendCodes.size());

        // Verify that only recent friend codes remain
        assertTrue(remainingFriendCodes.stream()
                .anyMatch(fc -> "333333333333".equals(fc.getFriendCode())));
        assertTrue(remainingFriendCodes.stream()
                .anyMatch(fc -> "444444444444".equals(fc.getFriendCode())));

        // Verify that old friend codes were deleted
        assertFalse(remainingFriendCodes.stream()
                .anyMatch(fc -> "111111111111".equals(fc.getFriendCode())));
        assertFalse(remainingFriendCodes.stream()
                .anyMatch(fc -> "222222222222".equals(fc.getFriendCode())));
    }

    @Test
    @DisplayName("Should handle empty database gracefully")
    void shouldHandleEmptyDatabaseGracefully() {
        // Given - empty database
        assertEquals(0, friendCodeRepository.count());

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> friendCodeCleanupService.cleanupOldFriendCodes());
        
        // Still empty
        assertEquals(0, friendCodeRepository.count());
    }

    @Test
    @DisplayName("Should handle case where no friend codes are old enough for deletion")
    void shouldHandleNoOldFriendCodes() {
        // Given - Create only recent friend codes
        FriendCode recentFriendCode1 = new FriendCode("111111111111", "RecentTrainer1");
        FriendCode recentFriendCode2 = new FriendCode("222222222222", "RecentTrainer2");

        entityManager.persistAndFlush(recentFriendCode1);
        entityManager.persistAndFlush(recentFriendCode2);

        // Verify initial state
        assertEquals(2, friendCodeRepository.count());

        // When - trigger cleanup
        friendCodeCleanupService.cleanupOldFriendCodes();

        // Then - all friend codes should remain
        assertEquals(2, friendCodeRepository.count());
    }
}
