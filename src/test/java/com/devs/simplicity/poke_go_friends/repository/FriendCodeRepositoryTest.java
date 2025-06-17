package com.devs.simplicity.poke_go_friends.repository;

import com.devs.simplicity.poke_go_friends.model.FriendCode;
import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for FriendCodeRepository.
 * Uses @DataJpaTest to test only the JPA layer with an in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FriendCode Repository Tests")
class FriendCodeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FriendCodeRepository friendCodeRepository;

    private FriendCode activeFriendCode;
    private FriendCode expiredFriendCode;
    private final String testFingerprint = "test-fingerprint-123";

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        activeFriendCode = FriendCode.builder()
                .friendCode("123456789012")
                .trainerName("ActiveTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .submittedAt(now.minusHours(1))
                .expiresAt(now.plusHours(47))
                .userFingerprint(testFingerprint)
                .build();

        expiredFriendCode = FriendCode.builder()
                .friendCode("987654321098")
                .trainerName("ExpiredTrainer")
                .trainerLevel(30)
                .team(Team.VALOR)
                .country("Canada")
                .purpose(Purpose.GIFTS)
                .message("This code has expired")
                .submittedAt(now.minusHours(49))
                .expiresAt(now.minusHours(1))
                .userFingerprint("expired-fingerprint-456")
                .build();

        entityManager.persistAndFlush(activeFriendCode);
        entityManager.persistAndFlush(expiredFriendCode);
    }

    @Test
    @DisplayName("findActiveFriendCodes should return only non-expired codes")
    void findActiveFriendCodes_shouldReturnOnlyNonExpiredCodes() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodes(currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFriendCode()).isEqualTo("123456789012");
        assertThat(result.getContent().get(0).getTrainerName()).isEqualTo("ActiveTrainer");
    }

    @Test
    @DisplayName("findActiveFriendCodes should return codes ordered by submission date descending")
    void findActiveFriendCodes_shouldReturnCodesOrderedBySubmissionDateDescending() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // Create another active friend code with a more recent submission
        FriendCode newerActiveFriendCode = FriendCode.builder()
                .friendCode("111222333444")
                .trainerName("NewerTrainer")
                .submittedAt(now.minusMinutes(30))
                .expiresAt(now.plusHours(47))
                .userFingerprint("newer-fingerprint-789")
                .build();
        
        entityManager.persistAndFlush(newerActiveFriendCode);
        
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodes(now, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTrainerName()).isEqualTo("NewerTrainer");
        assertThat(result.getContent().get(1).getTrainerName()).isEqualTo("ActiveTrainer");
    }

    @Test
    @DisplayName("findMostRecentByUserFingerprint should return the most recent submission")
    void findMostRecentByUserFingerprint_shouldReturnMostRecentSubmission() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // Create another friend code with the same fingerprint but older submission
        FriendCode olderFriendCode = FriendCode.builder()
                .friendCode("555666777888")
                .trainerName("OlderSubmission")
                .submittedAt(now.minusHours(3))
                .expiresAt(now.plusHours(45))
                .userFingerprint(testFingerprint)
                .build();
        
        entityManager.persistAndFlush(olderFriendCode);

        // When
        FriendCode result = friendCodeRepository.findMostRecentByUserFingerprint(testFingerprint);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTrainerName()).isEqualTo("ActiveTrainer");
        assertThat(result.getFriendCode()).isEqualTo("123456789012");
    }

    @Test
    @DisplayName("countSubmissionsByUserSince should count submissions within time window")
    void countSubmissionsByUserSince_shouldCountSubmissionsWithinTimeWindow() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(2);

        // When
        long count = friendCodeRepository.countSubmissionsByUserSince(testFingerprint, since);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countSubmissionsByUserSince should return zero when no submissions in window")
    void countSubmissionsByUserSince_shouldReturnZeroWhenNoSubmissionsInWindow() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusMinutes(30);

        // When
        long count = friendCodeRepository.countSubmissionsByUserSince(testFingerprint, since);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("deleteExpiredFriendCodes should remove expired codes")
    void deleteExpiredFriendCodes_shouldRemoveExpiredCodes() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();
        
        // Verify initial state
        assertThat(friendCodeRepository.findAll()).hasSize(2);

        // When
        int deletedCount = friendCodeRepository.deleteExpiredFriendCodes(currentTime);

        // Then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(friendCodeRepository.findAll()).hasSize(1);
        assertThat(friendCodeRepository.findAll().get(0).getTrainerName()).isEqualTo("ActiveTrainer");
    }

    @Test
    @DisplayName("countActiveFriendCodes should return count of non-expired codes")
    void countActiveFriendCodes_shouldReturnCountOfNonExpiredCodes() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();

        // When
        long count = friendCodeRepository.countActiveFriendCodes(currentTime);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("save should persist friend code with all fields")
    void save_shouldPersistFriendCodeWithAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FriendCode newFriendCode = FriendCode.builder()
                .friendCode("999888777666")
                .trainerName("TestTrainer")
                .trainerLevel(40)
                .team(Team.INSTINCT)
                .country("Australia")
                .purpose(Purpose.RAIDS)
                .message("Looking for raid partners!")
                .submittedAt(now)
                .expiresAt(now.plusHours(48))
                .userFingerprint("new-test-fingerprint")
                .build();

        // When
        FriendCode saved = friendCodeRepository.save(newFriendCode);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFriendCode()).isEqualTo("999888777666");
        assertThat(saved.getTrainerName()).isEqualTo("TestTrainer");
        assertThat(saved.getTrainerLevel()).isEqualTo(40);
        assertThat(saved.getTeam()).isEqualTo(Team.INSTINCT);
        assertThat(saved.getCountry()).isEqualTo("Australia");
        assertThat(saved.getPurpose()).isEqualTo(Purpose.RAIDS);
        assertThat(saved.getMessage()).isEqualTo("Looking for raid partners!");
        assertThat(saved.getSubmittedAt()).isEqualTo(now);
        assertThat(saved.getExpiresAt()).isEqualTo(now.plusHours(48));
        assertThat(saved.getUserFingerprint()).isEqualTo("new-test-fingerprint");
    }

    @Test
    @DisplayName("findById should return friend code when exists")
    void findById_shouldReturnFriendCodeWhenExists() {
        // Given
        var friendCodeId = activeFriendCode.getId();

        // When
        var result = friendCodeRepository.findById(friendCodeId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFriendCode()).isEqualTo("123456789012");
    }

    @Test
    @DisplayName("findById should return empty when not exists")
    void findById_shouldReturnEmptyWhenNotExists() {
        // Given
        var nonExistentId = java.util.UUID.randomUUID();

        // When
        var result = friendCodeRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }
}
