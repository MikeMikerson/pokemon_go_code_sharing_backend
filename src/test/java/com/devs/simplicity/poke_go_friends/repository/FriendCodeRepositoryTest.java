package com.devs.simplicity.poke_go_friends.repository;

import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeRepository.
 * Tests all custom queries and repository methods.
 */
@DataJpaTest
@ActiveProfiles("test")
class FriendCodeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FriendCodeRepository friendCodeRepository;

    private User testUser;
    private FriendCode activeFriendCode1;
    private FriendCode activeFriendCode2;
    private FriendCode inactiveFriendCode;
    private FriendCode expiredFriendCode;
    private LocalDateTime currentTime;

    @BeforeEach
    void setUp() {
        currentTime = LocalDateTime.now();
        
        // Create test user
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setTrainerName("TestTrainer");
        testUser.setPlayerLevel(25);
        testUser.setLocation("New York");
        entityManager.persistAndFlush(testUser);

        // Create active friend codes
        activeFriendCode1 = new FriendCode("123456789012", "ActiveTrainer1", 30, "Los Angeles", "Looking for daily gifts");
        activeFriendCode1.setUser(testUser);
        entityManager.persistAndFlush(activeFriendCode1);

        activeFriendCode2 = new FriendCode("234567890123", "ActiveTrainer2", 40, "New York", "Raiding partner needed");
        entityManager.persistAndFlush(activeFriendCode2);

        // Create inactive friend code
        inactiveFriendCode = new FriendCode("345678901234", "InactiveTrainer", 20, "Chicago", "Not looking anymore");
        inactiveFriendCode.deactivate();
        entityManager.persistAndFlush(inactiveFriendCode);

        // Create expired friend code
        expiredFriendCode = new FriendCode("456789012345", "ExpiredTrainer", 35, "Miami", "Expired code");
        expiredFriendCode.setExpiration(currentTime.minusDays(1)); // Expired yesterday
        entityManager.persistAndFlush(expiredFriendCode);

        entityManager.clear();
    }

    @Test
    void findActiveFriendCodes_shouldReturnOnlyActiveNonExpiredCodes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodes(currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(FriendCode::getFriendCode)
            .containsExactly("234567890123", "123456789012"); // Ordered by creation time desc
    }

    @Test
    void findActiveFriendCodesByLocation_shouldReturnCodesMatchingLocation() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - case insensitive search
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesByLocation("new york", currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLocation()).isEqualTo("New York");
    }

    @Test
    void findActiveFriendCodesByLevelRange_shouldReturnCodesInRange() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesByLevelRange(25, 35, currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPlayerLevel()).isEqualTo(30);
    }

    @Test
    void findRecentSubmissions_shouldReturnCodesCreatedAfterGivenTime() {
        // Given
        LocalDateTime since = currentTime.minusHours(1);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<FriendCode> result = friendCodeRepository.findRecentSubmissions(since, pageable);

        // Then
        assertThat(result.getContent()).hasSize(4); // All codes are recent in test
    }

    @Test
    void findActiveFriendCodesByTrainerName_shouldReturnMatchingTrainerNames() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - case insensitive search
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesByTrainerName("activetrainer1", currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTrainerName()).isEqualTo("ActiveTrainer1");
    }

    @Test
    void findActiveFriendCodesByDescription_shouldReturnMatchingDescriptions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - case insensitive search
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesByDescription("daily", currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).contains("daily gifts");
    }

    @Test
    void findByUserOrderByCreatedAtDesc_shouldReturnUsersFriendCodes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<FriendCode> result = friendCodeRepository.findByUserOrderByCreatedAtDesc(testUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser()).isEqualTo(testUser);
    }

    @Test
    void findActiveFriendCodesByUser_shouldReturnActiveUserFriendCodes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesByUser(testUser, currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser()).isEqualTo(testUser);
        assertThat(result.getContent().get(0).isCurrentlyActive()).isTrue();
    }

    @Test
    void findByFriendCode_shouldReturnExistingFriendCode() {
        // When
        Optional<FriendCode> result = friendCodeRepository.findByFriendCode("123456789012");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTrainerName()).isEqualTo("ActiveTrainer1");
    }

    @Test
    void findByFriendCode_shouldReturnEmptyForNonExistentCode() {
        // When
        Optional<FriendCode> result = friendCodeRepository.findByFriendCode("999999999999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findExpiredActiveFriendCodes_shouldReturnExpiredButActiveCodes() {
        // When
        List<FriendCode> result = friendCodeRepository.findExpiredActiveFriendCodes(currentTime);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFriendCode()).isEqualTo("456789012345");
        assertThat(result.get(0).getIsActive()).isTrue();
        assertThat(result.get(0).getExpiresAt()).isBefore(currentTime);
    }

    @Test
    void countActiveFriendCodes_shouldReturnCorrectCount() {
        // When
        Long count = friendCodeRepository.countActiveFriendCodes(currentTime);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByUser_shouldReturnUserFriendCodeCount() {
        // When
        Long count = friendCodeRepository.countByUser(testUser);

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countActiveFriendCodesByUser_shouldReturnActiveUserFriendCodeCount() {
        // When
        Long count = friendCodeRepository.countActiveFriendCodesByUser(testUser, currentTime);

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void findActiveFriendCodesWithFilters_shouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search with location filter
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesWithFilters(
            "Los Angeles", null, null, null, currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLocation()).isEqualTo("Los Angeles");
    }

    @Test
    void findActiveFriendCodesWithFilters_shouldReturnResultsWithLevelFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search with level range filter
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesWithFilters(
            null, 35, 45, null, currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPlayerLevel()).isEqualTo(40);
    }

    @Test
    void findActiveFriendCodesWithFilters_shouldReturnResultsWithTextSearch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search with text filter
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesWithFilters(
            null, null, null, "raiding", currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).contains("Raiding");
    }

    @Test
    void findActiveFriendCodesWithFilters_shouldReturnAllWhenNoFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search with no filters
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesWithFilters(
            null, null, null, null, currentTime, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findActiveFriendCodesWithFilters_shouldReturnEmptyWhenNoMatches() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search with filter that matches nothing
        Page<FriendCode> result = friendCodeRepository.findActiveFriendCodesWithFilters(
            "NonExistentCity", null, null, null, currentTime, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void pagination_shouldWorkCorrectly() {
        // Given - create more test data
        for (int i = 0; i < 15; i++) {
            FriendCode fc = new FriendCode(String.format("%012d", 500000000000L + i), "Trainer" + i);
            entityManager.persistAndFlush(fc);
        }
        entityManager.clear();

        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        // When
        Page<FriendCode> firstPageResult = friendCodeRepository.findActiveFriendCodes(currentTime, firstPage);
        Page<FriendCode> secondPageResult = friendCodeRepository.findActiveFriendCodes(currentTime, secondPage);

        // Then
        assertThat(firstPageResult.getContent()).hasSize(5);
        assertThat(secondPageResult.getContent()).hasSize(5);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(17L); // 2 original + 15 new
        assertThat(firstPageResult.getTotalPages()).isEqualTo(4);
        assertThat(firstPageResult.isFirst()).isTrue();
        assertThat(secondPageResult.isFirst()).isFalse();
    }
}
