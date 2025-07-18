package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FriendCodeCleanupService.
 * Tests the scheduled cleanup functionality for old friend codes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FriendCodeCleanupService Tests")
class FriendCodeCleanupServiceTest {

    @Mock
    private FriendCodeRepository friendCodeRepository;

    @InjectMocks
    private FriendCodeCleanupService friendCodeCleanupService;

    @Test
    @DisplayName("Should delete friend codes older than 24 hours")
    void shouldDeleteFriendCodesOlderThan24Hours() {
        // Given
        when(friendCodeRepository.deleteByCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(5); // 5 friend codes deleted

        // When
        friendCodeCleanupService.cleanupOldFriendCodes();

        // Then
        ArgumentCaptor<LocalDateTime> timestampCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(friendCodeRepository).deleteByCreatedAtBefore(timestampCaptor.capture());
        
        // Verify that the timestamp passed is approximately 24 hours ago
        LocalDateTime capturedTimestamp = timestampCaptor.getValue();
        LocalDateTime expectedTimestamp = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        
        // Allow for a small margin due to test execution time (within 5 seconds)
        assertTrue(capturedTimestamp.isAfter(expectedTimestamp.minusSeconds(5)));
        assertTrue(capturedTimestamp.isBefore(expectedTimestamp.plusSeconds(5)));
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void shouldHandleRepositoryExceptionsGracefully() {
        // Given
        when(friendCodeRepository.deleteByCreatedAtBefore(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> friendCodeCleanupService.cleanupOldFriendCodes());

        verify(friendCodeRepository).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }
}
