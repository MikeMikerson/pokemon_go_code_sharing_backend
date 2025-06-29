package com.devs.simplicity.poke_go_friends.repository;

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
 * Unit tests for UserRepository.
 * Tests all custom queries and repository methods.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User activeUser1;
    private User activeUser2;
    private User inactiveUser;
    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        // Create active users
        activeUser1 = new User("activeuser1", "active1@example.com", "hashedpass1");
        activeUser1.setTrainerName("ActiveTrainer1");
        activeUser1.setPlayerLevel(25);
        activeUser1.setLocation("New York");
        activeUser1.verifyEmail();
        activeUser1.updateLastLogin();
        entityManager.persistAndFlush(activeUser1);

        activeUser2 = new User("activeuser2", "active2@example.com", "hashedpass2");
        activeUser2.setTrainerName("ActiveTrainer2");
        activeUser2.setPlayerLevel(35);
        activeUser2.setLocation("Los Angeles");
        activeUser2.verifyEmail();
        entityManager.persistAndFlush(activeUser2);

        // Create inactive user
        inactiveUser = new User("inactiveuser", "inactive@example.com", "hashedpass3");
        inactiveUser.setTrainerName("InactiveTrainer");
        inactiveUser.deactivate();
        entityManager.persistAndFlush(inactiveUser);

        // Create unverified user
        unverifiedUser = new User("unverifieduser", "unverified@example.com", "hashedpass4");
        unverifiedUser.setTrainerName("UnverifiedTrainer");
        // emailVerified remains false by default
        entityManager.persistAndFlush(unverifiedUser);

        entityManager.clear();
    }

    @Test
    void findByUsername_shouldReturnUserWhenExists() {
        // When
        Optional<User> result = userRepository.findByUsername("activeuser1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("active1@example.com");
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        // When
        Optional<User> result = userRepository.findByEmail("active1@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("activeuser1");
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotExists() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameOrEmail_shouldReturnUserByUsername() {
        // When
        Optional<User> result = userRepository.findByUsernameOrEmail("activeuser1", "wrong@email.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("activeuser1");
    }

    @Test
    void findByUsernameOrEmail_shouldReturnUserByEmail() {
        // When
        Optional<User> result = userRepository.findByUsernameOrEmail("wrongusername", "active1@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("active1@example.com");
    }

    @Test
    void existsByUsername_shouldReturnTrueWhenExists() {
        // When
        boolean exists = userRepository.existsByUsername("activeuser1");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_shouldReturnTrueWhenExists() {
        // When
        boolean exists = userRepository.existsByEmail("active1@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByIsActiveTrueOrderByCreatedAtDesc_shouldReturnOnlyActiveUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);

        // Then
        assertThat(result.getContent()).hasSize(3); // 2 active + 1 unverified (but active)
        assertThat(result.getContent())
            .extracting(User::getUsername)
            .containsExactly("unverifieduser", "activeuser2", "activeuser1"); // Ordered by creation desc
    }

    @Test
    void findActiveUsersByTrainerName_shouldReturnMatchingTrainerNames() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - case insensitive search
        Page<User> result = userRepository.findActiveUsersByTrainerName("activetrainer1", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTrainerName()).isEqualTo("ActiveTrainer1");
    }

    @Test
    void findActiveUsersByLocation_shouldReturnMatchingLocations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - case insensitive search
        Page<User> result = userRepository.findActiveUsersByLocation("new york", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLocation()).isEqualTo("New York");
    }

    @Test
    void findByEmailVerifiedFalseAndIsActiveTrue_shouldReturnUnverifiedUsers() {
        // When
        List<User> result = userRepository.findByEmailVerifiedFalseAndIsActiveTrue();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("unverifieduser");
        assertThat(result.get(0).getEmailVerified()).isFalse();
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    @Test
    void findUsersInactiveSince_shouldReturnUsersWithoutRecentLogin() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);

        // When
        List<User> result = userRepository.findUsersInactiveSince(cutoffDate);

        // Then
        assertThat(result).hasSize(2); // activeuser2 and unverifieduser (no last login set)
        assertThat(result)
            .extracting(User::getUsername)
            .containsExactlyInAnyOrder("activeuser2", "unverifieduser");
    }

    @Test
    void countByIsActiveTrue_shouldReturnCorrectCount() {
        // When
        Long count = userRepository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(3L); // 2 active + 1 unverified (but active)
    }

    @Test
    void countByEmailVerifiedTrueAndIsActiveTrue_shouldReturnVerifiedActiveCount() {
        // When
        Long count = userRepository.countByEmailVerifiedTrueAndIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(2L); // Only activeuser1 and activeuser2 are verified and active
    }

    @Test
    void pagination_shouldWorkCorrectlyForActiveUsers() {
        // Given - create more test data
        for (int i = 0; i < 8; i++) {
            User user = new User("user" + i, "user" + i + "@example.com", "hashedpass");
            user.setTrainerName("Trainer" + i);
            entityManager.persistAndFlush(user);
        }
        entityManager.clear();

        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        // When
        Page<User> firstPageResult = userRepository.findByIsActiveTrueOrderByCreatedAtDesc(firstPage);
        Page<User> secondPageResult = userRepository.findByIsActiveTrueOrderByCreatedAtDesc(secondPage);

        // Then
        assertThat(firstPageResult.getContent()).hasSize(5);
        assertThat(secondPageResult.getContent()).hasSize(5);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(11L); // 3 original + 8 new
        assertThat(firstPageResult.getTotalPages()).isEqualTo(3);
        assertThat(firstPageResult.isFirst()).isTrue();
        assertThat(secondPageResult.isFirst()).isFalse();
    }

    @Test
    void findActiveUsersByTrainerName_shouldReturnEmptyWhenNoMatch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findActiveUsersByTrainerName("NonExistentTrainer", pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findActiveUsersByLocation_shouldReturnEmptyWhenNoMatch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findActiveUsersByLocation("NonExistentCity", pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }
}
