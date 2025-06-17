package com.devs.simplicity.poke_go_friends.repository;

import com.devs.simplicity.poke_go_friends.model.FriendCode;
import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FriendCodeRepositoryIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FriendCodeRepository friendCodeRepository;

    @Test
    void save_validFriendCode_returnsSavedEntity() {
        // Given
        FriendCode friendCode = FriendCode.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .userFingerprint("test-fingerprint")
                .build();

        // When
        FriendCode saved = friendCodeRepository.save(friendCode);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFriendCode()).isEqualTo("123456789012");
        assertThat(saved.getTrainerName()).isEqualTo("TestTrainer");
        assertThat(saved.getSubmittedAt()).isNotNull();
        assertThat(saved.getExpiresAt()).isNotNull();
        assertThat(saved.getExpiresAt()).isAfter(saved.getSubmittedAt());
    }

    @Test
    void findById_existingId_returnsOptionalWithFriendCode() {
        // Given
        FriendCode friendCode = createTestFriendCode();
        FriendCode saved = entityManager.persistAndFlush(friendCode);

        // When
        Optional<FriendCode> found = friendCodeRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFriendCode()).isEqualTo("123456789012");
    }

    @Test
    void findById_nonExistingId_returnsEmptyOptional() {
        // Given
        UUID nonExistingId = UUID.randomUUID();

        // When
        Optional<FriendCode> found = friendCodeRepository.findById(nonExistingId);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAllByExpiresAtAfterOrderBySubmittedAtDesc_validDateTime_returnsNonExpiredCodes() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        FriendCode expiredCode = createTestFriendCode();
        expiredCode.setExpiresAt(now.minusHours(1));
        entityManager.persistAndFlush(expiredCode);
        
        FriendCode validCode = createTestFriendCode();
        validCode.setFriendCode("987654321098");
        validCode.setExpiresAt(now.plusHours(1));
        entityManager.persistAndFlush(validCode);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        List<FriendCode> result = friendCodeRepository
                .findAllByExpiresAtAfterOrderBySubmittedAtDesc(now, pageable);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFriendCode()).isEqualTo("987654321098");
    }

    @Test
    void findByUserFingerprintAndSubmittedAtAfter_validFingerprint_returnsMatchingCodes() {
        // Given
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        String fingerprint = "test-fingerprint";
        
        FriendCode recentCode = createTestFriendCode();
        recentCode.setUserFingerprint(fingerprint);
        recentCode.setSubmittedAt(LocalDateTime.now().minusHours(1));
        entityManager.persistAndFlush(recentCode);
        
        FriendCode oldCode = createTestFriendCode();
        oldCode.setUserFingerprint(fingerprint);
        oldCode.setFriendCode("987654321098");
        oldCode.setSubmittedAt(LocalDateTime.now().minusHours(25));
        entityManager.persistAndFlush(oldCode);

        // When
        List<FriendCode> result = friendCodeRepository
                .findByUserFingerprintAndSubmittedAtAfter(fingerprint, cutoff);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFriendCode()).isEqualTo("123456789012");
    }

    @Test
    void deleteByExpiresAtBefore_validDateTime_deletesExpiredCodes() {
        // Given
        LocalDateTime cutoff = LocalDateTime.now();
        
        FriendCode expiredCode = createTestFriendCode();
        expiredCode.setExpiresAt(cutoff.minusHours(1));
        entityManager.persistAndFlush(expiredCode);
        
        FriendCode validCode = createTestFriendCode();
        validCode.setFriendCode("987654321098");
        validCode.setExpiresAt(cutoff.plusHours(1));
        entityManager.persistAndFlush(validCode);

        // When
        int deletedCount = friendCodeRepository.deleteByExpiresAtBefore(cutoff);

        // Then
        assertThat(deletedCount).isEqualTo(1);
        
        List<FriendCode> remaining = friendCodeRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getFriendCode()).isEqualTo("987654321098");
    }

    private FriendCode createTestFriendCode() {
        return FriendCode.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Test message")
                .userFingerprint("test-fingerprint")
                .build();
    }
}
