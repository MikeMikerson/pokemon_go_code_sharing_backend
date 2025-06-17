package com.devs.simplicity.poke_go_friends.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CleanupService Integration Test")
class CleanupServiceIT {

    @Autowired
    private CleanupService cleanupService;

    @Test
    @DisplayName("should be injectable as Spring bean")
    void cleanupService_springContext_shouldBeInjectable() {
        // Assert
        assertThat(cleanupService).isNotNull();
    }

    @Test
    @DisplayName("should handle cleanup without errors")
    void cleanupExpiredFriendCodes_springContext_shouldNotThrowExceptions() {
        // Act & Assert - Should not throw any exceptions
        int deletedCount = cleanupService.cleanupExpiredFriendCodes();
        
        // Should return a valid count (could be 0 if no expired codes)
        assertThat(deletedCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("should handle scheduled cleanup without errors")
    void scheduledCleanup_springContext_shouldNotThrowExceptions() {
        // Act & Assert - Should not throw any exceptions
        cleanupService.scheduledCleanup();
    }
}
