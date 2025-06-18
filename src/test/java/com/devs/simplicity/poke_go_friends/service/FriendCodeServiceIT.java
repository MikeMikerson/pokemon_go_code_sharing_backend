package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.MockCacheConfig;
import com.devs.simplicity.poke_go_friends.config.MockRedisConfig;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeFeedResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.dto.SubmissionResponse;
import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for FriendCodeService.
 * Tests the service in a real Spring context with actual repositories.
 */
@SpringBootTest(classes = {MockCacheConfig.class})
@ActiveProfiles("test")
@Transactional
@DisplayName("FriendCode Service Integration Tests")
class FriendCodeServiceIT {

    @Autowired
    private FriendCodeService friendCodeService;

    @Autowired
    private FriendCodeRepository friendCodeRepository;

    private FriendCodeSubmissionRequest validRequest;
    private String testFingerprint;

    @BeforeEach
    void setUp() {
        testFingerprint = "integration-test-fingerprint";
        
        validRequest = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("IntegrationTrainer")
                .trainerLevel(30)
                .team(Team.VALOR)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Integration test friend code")
                .build();
        
        // Clean up any existing data
        friendCodeRepository.deleteAll();
    }

    @Test
    @DisplayName("submitFriendCode should successfully save and return friend code")
    void submitFriendCode_integrationTest_shouldSaveAndReturnFriendCode() {
        // When
        SubmissionResponse response = friendCodeService.submitFriendCode(validRequest, testFingerprint);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getFriendCode()).isNotNull();
        assertThat(response.getFriendCode().getFriendCode()).isEqualTo("123456789012");
        assertThat(response.getFriendCode().getTrainerName()).isEqualTo("IntegrationTrainer");
        assertThat(response.getNextSubmissionAllowed()).isNotNull();

        // Verify it was actually saved to the database
        long count = friendCodeRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("getActiveFriendCodes should return submitted friend codes")
    void getActiveFriendCodes_integrationTest_shouldReturnSubmittedCodes() {
        // Given - submit a friend code first
        friendCodeService.submitFriendCode(validRequest, testFingerprint);

        // When
        FriendCodeFeedResponse response = friendCodeService.getActiveFriendCodes(0, 10);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFriendCodes()).hasSize(1);
        assertThat(response.getFriendCodes().get(0).getFriendCode()).isEqualTo("123456789012");
        assertThat(response.isHasMore()).isFalse();
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("countActiveFriendCodes should return correct count")
    void countActiveFriendCodes_integrationTest_shouldReturnCorrectCount() {
        // Given
        assertThat(friendCodeService.countActiveFriendCodes()).isEqualTo(0);

        // When - submit a friend code
        friendCodeService.submitFriendCode(validRequest, testFingerprint);

        // Then
        assertThat(friendCodeService.countActiveFriendCodes()).isEqualTo(1);
    }

    @Test
    @DisplayName("validateSubmissionRequest should validate according to constraints")
    void validateSubmissionRequest_integrationTest_shouldValidateConstraints() {
        // Valid request should pass
        assertThat(friendCodeService.validateSubmissionRequest(validRequest)).isTrue();

        // Invalid request should fail
        FriendCodeSubmissionRequest invalidRequest = FriendCodeSubmissionRequest.builder()
                .friendCode("invalid") // Too short
                .build();
        
        assertThat(friendCodeService.validateSubmissionRequest(invalidRequest)).isFalse();
    }

    @Test
    @DisplayName("hasExpiredCodes should work with real timestamps")
    void hasExpiredCodes_integrationTest_shouldWorkWithRealTimestamps() {
        // Given - no codes initially
        assertThat(friendCodeService.hasExpiredCodes()).isFalse();

        // When - submit a valid code (will expire in 48 hours)
        friendCodeService.submitFriendCode(validRequest, testFingerprint);

        // Then - should still have no expired codes
        assertThat(friendCodeService.hasExpiredCodes()).isFalse();
    }
}
