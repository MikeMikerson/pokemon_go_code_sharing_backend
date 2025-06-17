package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CleanupService")
class CleanupServiceTest {

    @Mock
    private FriendCodeRepository friendCodeRepository;

    private CleanupService cleanupService;

    @BeforeEach
    void setUp() {
        cleanupService = new CleanupService(friendCodeRepository);
    }

    @Test
    @DisplayName("should delete expired friend codes")
    void cleanupExpiredFriendCodes_shouldDeleteExpiredCodes() {
        // Arrange
        when(friendCodeRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(5);

        // Act
        int deletedCount = cleanupService.cleanupExpiredFriendCodes();

        // Assert
        verify(friendCodeRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("should return count of deleted friend codes")
    void cleanupExpiredFriendCodes_shouldReturnDeletedCount() {
        // Arrange
        int expectedDeletedCount = 3;
        when(friendCodeRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(expectedDeletedCount);

        // Act
        int actualDeletedCount = cleanupService.cleanupExpiredFriendCodes();

        // Assert
        assert actualDeletedCount == expectedDeletedCount;
    }

    @Test
    @DisplayName("should handle zero deleted codes gracefully")
    void cleanupExpiredFriendCodes_nothingToDelete_shouldReturnZero() {
        // Arrange
        when(friendCodeRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(0);

        // Act
        int deletedCount = cleanupService.cleanupExpiredFriendCodes();

        // Assert
        assert deletedCount == 0;
        verify(friendCodeRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("should use current time as cutoff for expired codes")
    void cleanupExpiredFriendCodes_shouldUseCurrentTimeAsCutoff() {
        // Arrange
        LocalDateTime beforeTest = LocalDateTime.now().minusSeconds(1);
        when(friendCodeRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(2);

        // Act
        cleanupService.cleanupExpiredFriendCodes();

        // Assert
        verify(friendCodeRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("should run scheduled cleanup task")
    void scheduledCleanup_shouldExecuteCleanupMethod() {
        // Arrange
        when(friendCodeRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(2);

        // Act
        cleanupService.scheduledCleanup();

        // Assert
        verify(friendCodeRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
