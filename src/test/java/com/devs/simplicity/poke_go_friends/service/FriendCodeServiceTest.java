package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.entity.User;
import com.devs.simplicity.poke_go_friends.exception.DuplicateFriendCodeException;
import com.devs.simplicity.poke_go_friends.exception.FriendCodeNotFoundException;
import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import com.devs.simplicity.poke_go_friends.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for FriendCodeService.
 */
@ExtendWith(MockitoExtension.class)
class FriendCodeServiceTest {

    @Mock
    private FriendCodeRepository friendCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private FriendCodeService friendCodeService;

    private FriendCode testFriendCode;
    private User testUser;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setId(1L);

        testFriendCode = new FriendCode("123456789012", "TestTrainer");
        testFriendCode.setId(1L);
        testFriendCode.setUser(testUser);

        testPageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Create Friend Code")
    class CreateFriendCodeTest {

        @Test
        @DisplayName("Should create friend code successfully")
        void shouldCreateFriendCodeSuccessfully() {
            // Given
            String friendCode = "123456789012";
            String trainerName = "TestTrainer";
            String ipAddress = "192.168.1.1";
            Long userId = 1L;

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(friendCodeRepository.findByFriendCode(friendCode)).thenReturn(Optional.empty());
            when(friendCodeRepository.save(any(FriendCode.class))).thenReturn(testFriendCode);

            // When
            FriendCode result = friendCodeService.createFriendCode(
                friendCode, trainerName, 25, "New York", "Looking for friends", null, null, ipAddress, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFriendCode()).isEqualTo(friendCode);
            assertThat(result.getTrainerName()).isEqualTo(trainerName);

            verify(validationService).validateFriendCodeSubmission(
                friendCode, trainerName, 25, "New York", "Looking for friends", ipAddress, userId);
            verify(friendCodeRepository).findByFriendCode(friendCode);
            verify(friendCodeRepository).save(any(FriendCode.class));
        }

        @Test
        @DisplayName("Should create anonymous friend code")
        void shouldCreateAnonymousFriendCode() {
            // Given
            String friendCode = "123456789012";
            String trainerName = "TestTrainer";
            String ipAddress = "192.168.1.1";

            when(friendCodeRepository.findByFriendCode(friendCode)).thenReturn(Optional.empty());
            when(friendCodeRepository.save(any(FriendCode.class))).thenReturn(testFriendCode);

            // When
            FriendCode result = friendCodeService.createFriendCode(
                friendCode, trainerName, null, null, null, null, null, ipAddress, null);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw exception when duplicate friend code exists")
        void shouldThrowExceptionWhenDuplicateFriendCodeExists() {
            // Given
            String friendCode = "123456789012";
            String trainerName = "TestTrainer";
            String ipAddress = "192.168.1.1";

            FriendCode existingCode = new FriendCode(friendCode, "ExistingTrainer");
            existingCode.setIsActive(true);

            when(friendCodeRepository.findByFriendCode(friendCode)).thenReturn(Optional.of(existingCode));

            // When & Then
            assertThatThrownBy(() -> friendCodeService.createFriendCode(
                friendCode, trainerName, null, null, null, null, null, ipAddress, null))
                .isInstanceOf(DuplicateFriendCodeException.class);

            verify(friendCodeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            String friendCode = "123456789012";
            String trainerName = "TestTrainer";
            String ipAddress = "192.168.1.1";
            Long userId = 999L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> friendCodeService.createFriendCode(
                friendCode, trainerName, null, null, null, null, null, ipAddress, userId))
                .isInstanceOf(FriendCodeNotFoundException.class)
                .hasMessageContaining("User not found");

            verify(friendCodeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create friend code with expiration")
        void shouldCreateFriendCodeWithExpiration() {
            // Given
            String friendCode = "123456789012";
            String trainerName = "TestTrainer";
            String ipAddress = "192.168.1.1";
            int expirationDays = 30;

            when(friendCodeRepository.findByFriendCode(friendCode)).thenReturn(Optional.empty());
            when(friendCodeRepository.save(any(FriendCode.class))).thenReturn(testFriendCode);

            // When
            FriendCode result = friendCodeService.createFriendCodeWithExpiration(
                friendCode, trainerName, null, null, null, null, null, ipAddress, null, expirationDays);

            // Then
            assertThat(result).isNotNull();
            verify(friendCodeRepository, times(2)).save(any(FriendCode.class)); // Once for creation, once for expiration
        }
    }

    @Nested
    @DisplayName("Retrieve Friend Code")
    class RetrieveFriendCodeTest {

        @Test
        @DisplayName("Should get friend code by ID")
        void shouldGetFriendCodeById() {
            // Given
            Long id = 1L;
            when(friendCodeRepository.findById(id)).thenReturn(Optional.of(testFriendCode));

            // When
            FriendCode result = friendCodeService.getFriendCodeById(id);

            // Then
            assertThat(result).isEqualTo(testFriendCode);
            verify(friendCodeRepository).findById(id);
        }

        @Test
        @DisplayName("Should throw exception when friend code not found by ID")
        void shouldThrowExceptionWhenFriendCodeNotFoundById() {
            // Given
            Long id = 999L;
            when(friendCodeRepository.findById(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> friendCodeService.getFriendCodeById(id))
                .isInstanceOf(FriendCodeNotFoundException.class)
                .hasMessageContaining("Friend code not found with ID: " + id);
        }

        @Test
        @DisplayName("Should get friend code by value")
        void shouldGetFriendCodeByValue() {
            // Given
            String friendCode = "123456789012";
            when(friendCodeRepository.findByFriendCode(friendCode)).thenReturn(Optional.of(testFriendCode));

            // When
            FriendCode result = friendCodeService.getFriendCodeByValue(friendCode);

            // Then
            assertThat(result).isEqualTo(testFriendCode);
            verify(validationService).validateFriendCodeFormat(friendCode);
            verify(friendCodeRepository).findByFriendCode(friendCode);
        }

        @Test
        @DisplayName("Should throw exception when friend code not found by value")
        void shouldThrowExceptionWhenFriendCodeNotFoundByValue() {
            // Given
            String friendCode = "123456789012";
            when(friendCodeRepository.findByFriendCode(friendCode)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> friendCodeService.getFriendCodeByValue(friendCode))
                .isInstanceOf(FriendCodeNotFoundException.class)
                .hasMessageContaining("Friend code not found with friendCode: " + friendCode);
        }
    }

    @Nested
    @DisplayName("List and Filter Friend Codes")
    class ListAndFilterFriendCodesTest {

        @Test
        @DisplayName("Should get active friend codes")
        void shouldGetActiveFriendCodes() {
            // Given
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(friendCodeRepository.findActiveFriendCodes(any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.getActiveFriendCodes(testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testFriendCode);
            verify(friendCodeRepository).findActiveFriendCodes(any(LocalDateTime.class), eq(testPageable));
        }

        @Test
        @DisplayName("Should filter friend codes by location")
        void shouldFilterFriendCodesByLocation() {
            // Given
            String location = "New York";
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(friendCodeRepository.findActiveFriendCodesByLocation(eq(location), any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.getFriendCodesByLocation(location, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(friendCodeRepository).findActiveFriendCodesByLocation(eq(location), any(LocalDateTime.class), eq(testPageable));
        }

        @Test
        @DisplayName("Should return all active codes when location is empty")
        void shouldReturnAllActiveCodesWhenLocationIsEmpty() {
            // Given
            String location = "";
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(friendCodeRepository.findActiveFriendCodes(any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.getFriendCodesByLocation(location, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(friendCodeRepository).findActiveFriendCodes(any(LocalDateTime.class), eq(testPageable));
            verify(friendCodeRepository, never()).findActiveFriendCodesByLocation(anyString(), any(LocalDateTime.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter friend codes by level range")
        void shouldFilterFriendCodesByLevelRange() {
            // Given
            Integer minLevel = 20;
            Integer maxLevel = 40;
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(friendCodeRepository.findActiveFriendCodesByLevelRange(eq(minLevel), eq(maxLevel), any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.getFriendCodesByLevelRange(minLevel, maxLevel, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(validationService).validatePlayerLevel(minLevel);
            verify(validationService).validatePlayerLevel(maxLevel);
            verify(friendCodeRepository).findActiveFriendCodesByLevelRange(eq(minLevel), eq(maxLevel), any(LocalDateTime.class), eq(testPageable));
        }

        @Test
        @DisplayName("Should search by trainer name")
        void shouldSearchByTrainerName() {
            // Given
            String trainerName = "TestTrainer";
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(friendCodeRepository.findActiveFriendCodesByTrainerName(eq(trainerName), any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.searchByTrainerName(trainerName, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(friendCodeRepository).findActiveFriendCodesByTrainerName(eq(trainerName), any(LocalDateTime.class), eq(testPageable));
        }

        @Test
        @DisplayName("Should search with filters")
        void shouldSearchWithFilters() {
            // Given
            String location = "New York";
            Integer minLevel = 20;
            Integer maxLevel = 40;
            String searchText = "daily";
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(friendCodeRepository.findActiveFriendCodesWithFilters(
                eq(location), eq(minLevel), eq(maxLevel), eq(searchText), any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.searchWithFilters(
                location, minLevel, maxLevel, searchText, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(validationService).validatePlayerLevel(minLevel);
            verify(validationService).validatePlayerLevel(maxLevel);
            verify(friendCodeRepository).findActiveFriendCodesWithFilters(
                eq(location), eq(minLevel), eq(maxLevel), eq(searchText), any(LocalDateTime.class), eq(testPageable));
        }
    }

    @Nested
    @DisplayName("User-Specific Operations")
    class UserSpecificOperationsTest {

        @Test
        @DisplayName("Should get friend codes by user")
        void shouldGetFriendCodesByUser() {
            // Given
            Long userId = 1L;
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(friendCodeRepository.findByUserOrderByCreatedAtDesc(testUser, testPageable))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.getFriendCodesByUser(userId, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).findById(userId);
            verify(friendCodeRepository).findByUserOrderByCreatedAtDesc(testUser, testPageable);
        }

        @Test
        @DisplayName("Should get active friend codes by user")
        void shouldGetActiveFriendCodesByUser() {
            // Given
            Long userId = 1L;
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(friendCodeRepository.findActiveFriendCodesByUser(eq(testUser), any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.getActiveFriendCodesByUser(userId, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).findById(userId);
            verify(friendCodeRepository).findActiveFriendCodesByUser(eq(testUser), any(LocalDateTime.class), eq(testPageable));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTest {

        @Test
        @DisplayName("Should deactivate friend code by owner")
        void shouldDeactivateFriendCodeByOwner() {
            // Given
            Long id = 1L;
            Long userId = 1L;
            
            when(friendCodeRepository.findById(id)).thenReturn(Optional.of(testFriendCode));
            when(friendCodeRepository.save(any(FriendCode.class))).thenReturn(testFriendCode);

            // When
            friendCodeService.deactivateFriendCode(id, userId);

            // Then
            verify(friendCodeRepository).save(testFriendCode);
        }

        @Test
        @DisplayName("Should set friend code expiration")
        void shouldSetFriendCodeExpiration() {
            // Given
            Long id = 1L;
            Long userId = 1L;
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
            
            when(friendCodeRepository.findById(id)).thenReturn(Optional.of(testFriendCode));
            when(friendCodeRepository.save(any(FriendCode.class))).thenReturn(testFriendCode);

            // When
            FriendCode result = friendCodeService.setFriendCodeExpiration(id, expiresAt, userId);

            // Then
            assertThat(result).isNotNull();
            verify(friendCodeRepository).save(testFriendCode);
        }
    }

    @Nested
    @DisplayName("Maintenance Operations")
    class MaintenanceOperationsTest {

        @Test
        @DisplayName("Should cleanup expired friend codes")
        void shouldCleanupExpiredFriendCodes() {
            // Given
            FriendCode expiredCode1 = new FriendCode("111111111111", "Expired1");
            FriendCode expiredCode2 = new FriendCode("222222222222", "Expired2");
            List<FriendCode> expiredCodes = Arrays.asList(expiredCode1, expiredCode2);
            
            when(friendCodeRepository.findExpiredActiveFriendCodes(any(LocalDateTime.class)))
                .thenReturn(expiredCodes);
            when(friendCodeRepository.save(any(FriendCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            int result = friendCodeService.cleanupExpiredFriendCodes();

            // Then
            assertThat(result).isEqualTo(2);
            verify(friendCodeRepository).findExpiredActiveFriendCodes(any(LocalDateTime.class));
            verify(friendCodeRepository, times(2)).save(any(FriendCode.class));
        }

        @Test
        @DisplayName("Should get statistics")
        void shouldGetStatistics() {
            // Given
            when(friendCodeRepository.countActiveFriendCodes(any(LocalDateTime.class))).thenReturn(10L);
            when(friendCodeRepository.count()).thenReturn(15L);

            // When
            FriendCodeService.FriendCodeStats result = friendCodeService.getStatistics();

            // Then
            assertThat(result.getActiveFriendCodes()).isEqualTo(10L);
            assertThat(result.getTotalFriendCodes()).isEqualTo(15L);
            assertThat(result.getInactiveFriendCodes()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should get recent submissions")
        void shouldGetRecentSubmissions() {
            // Given
            int hours = 24;
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, testPageable, 1);
            
            when(friendCodeRepository.findRecentSubmissions(any(LocalDateTime.class), eq(testPageable)))
                .thenReturn(page);

            // When
            Page<FriendCode> result = friendCodeService.getRecentSubmissions(hours, testPageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(friendCodeRepository).findRecentSubmissions(any(LocalDateTime.class), eq(testPageable));
        }
    }
}
