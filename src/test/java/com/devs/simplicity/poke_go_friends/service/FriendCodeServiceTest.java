package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.dto.FriendCodeFeedResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.dto.SubmissionResponse;
import com.devs.simplicity.poke_go_friends.mapper.FriendCodeMapper;
import com.devs.simplicity.poke_go_friends.model.FriendCode;
import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FriendCodeService.
 * Tests business logic and validation according to TDD principles.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FriendCode Service Tests")
class FriendCodeServiceTest {

    @Mock
    private FriendCodeRepository friendCodeRepository;

    @Mock
    private FriendCodeMapper friendCodeMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private FriendCodeService friendCodeService;

    private FriendCodeSubmissionRequest validRequest;
    private FriendCode validEntity;
    private FriendCodeResponse validResponse;
    private String testFingerprint;

    @BeforeEach
    void setUp() {
        testFingerprint = "test-fingerprint-123";
        
        validRequest = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .build();
        
        validEntity = FriendCode.builder()
                .id(UUID.randomUUID())
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .submittedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(48))
                .userFingerprint(testFingerprint)
                .build();
        
        validResponse = FriendCodeResponse.builder()
                .id(validEntity.getId())
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .submittedAt(validEntity.getSubmittedAt())
                .expiresAt(validEntity.getExpiresAt())
                .build();
    }

    @Test
    @DisplayName("submitFriendCode should return success when valid request is provided")
    void submitFriendCode_validRequest_shouldReturnSuccess() {
        // Given
        when(friendCodeMapper.toEntity(validRequest, testFingerprint)).thenReturn(validEntity);
        when(validator.validate(validEntity)).thenReturn(Collections.emptySet());
        when(friendCodeRepository.save(any(FriendCode.class))).thenReturn(validEntity);
        when(friendCodeMapper.toResponse(validEntity)).thenReturn(validResponse);

        // When
        SubmissionResponse response = friendCodeService.submitFriendCode(validRequest, testFingerprint);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Friend code submitted successfully");
        assertThat(response.getFriendCode()).isEqualTo(validResponse);
        assertThat(response.getNextSubmissionAllowed()).isNotNull();

        verify(friendCodeRepository).save(any(FriendCode.class));
        verify(friendCodeMapper).toEntity(validRequest, testFingerprint);
        verify(friendCodeMapper).toResponse(validEntity);
    }

    @Test
    @DisplayName("submitFriendCode should return validation error when entity validation fails")
    void submitFriendCode_invalidEntity_shouldReturnValidationError() {
        // Given
        ConstraintViolation<FriendCode> violation = createMockViolation("Friend code must be exactly 12 digits");
        when(friendCodeMapper.toEntity(validRequest, testFingerprint)).thenReturn(validEntity);
        when(validator.validate(validEntity)).thenReturn(Set.of(violation));

        // When
        SubmissionResponse response = friendCodeService.submitFriendCode(validRequest, testFingerprint);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Friend code must be exactly 12 digits");
        assertThat(response.getFriendCode()).isNull();

        verify(friendCodeRepository, never()).save(any(FriendCode.class));
    }

    @Test
    @DisplayName("submitFriendCode should return error when repository throws exception")
    void submitFriendCode_repositoryException_shouldReturnError() {
        // Given
        when(friendCodeMapper.toEntity(validRequest, testFingerprint)).thenReturn(validEntity);
        when(validator.validate(validEntity)).thenReturn(Collections.emptySet());
        when(friendCodeRepository.save(any(FriendCode.class))).thenThrow(new RuntimeException("Database error"));

        // When
        SubmissionResponse response = friendCodeService.submitFriendCode(validRequest, testFingerprint);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Internal error occurred while processing submission");
        assertThat(response.getFriendCode()).isNull();
    }

    @Test
    @DisplayName("getActiveFriendCodes should return paginated response with active codes")
    void getActiveFriendCodes_validPageRequest_shouldReturnPaginatedResponse() {
        // Given
        int page = 0;
        int size = 10;
        List<FriendCode> friendCodes = List.of(validEntity);
        Page<FriendCode> friendCodesPage = new PageImpl<>(friendCodes, PageRequest.of(page, size), 1);
        List<FriendCodeResponse> responseList = List.of(validResponse);

        when(friendCodeRepository.findActiveFriendCodes(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(friendCodesPage);
        when(friendCodeMapper.toResponseList(friendCodes)).thenReturn(responseList);

        // When
        FriendCodeFeedResponse response = friendCodeService.getActiveFriendCodes(page, size);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFriendCodes()).hasSize(1);
        assertThat(response.getFriendCodes().get(0)).isEqualTo(validResponse);
        assertThat(response.isHasMore()).isFalse();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isEqualTo(page);
        assertThat(response.getPageSize()).isEqualTo(size);

        verify(friendCodeRepository).findActiveFriendCodes(any(LocalDateTime.class), any(Pageable.class));
        verify(friendCodeMapper).toResponseList(friendCodes);
    }

    @Test
    @DisplayName("getActiveFriendCodes should indicate hasMore when multiple pages exist")
    void getActiveFriendCodes_multiplePages_shouldIndicateHasMore() {
        // Given
        int page = 0;
        int size = 10;
        List<FriendCode> friendCodes = List.of(validEntity);
        Page<FriendCode> friendCodesPage = new PageImpl<>(friendCodes, PageRequest.of(page, size), 25); // 25 total elements
        List<FriendCodeResponse> responseList = List.of(validResponse);

        when(friendCodeRepository.findActiveFriendCodes(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(friendCodesPage);
        when(friendCodeMapper.toResponseList(friendCodes)).thenReturn(responseList);

        // When
        FriendCodeFeedResponse response = friendCodeService.getActiveFriendCodes(page, size);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isHasMore()).isTrue();
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getNextCursor()).isEqualTo("1");
    }

    @Test
    @DisplayName("getFriendCodeById should return friend code when exists and not expired")
    void getFriendCodeById_existsAndNotExpired_shouldReturnFriendCode() {
        // Given
        String id = validEntity.getId().toString();
        FriendCode nonExpiredEntity = FriendCode.builder()
                .id(validEntity.getId())
                .friendCode("123456789012")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        when(friendCodeRepository.findById(validEntity.getId())).thenReturn(Optional.of(nonExpiredEntity));
        when(friendCodeMapper.toResponse(nonExpiredEntity)).thenReturn(validResponse);

        // When
        FriendCodeResponse response = friendCodeService.getFriendCodeById(id);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(validResponse);

        verify(friendCodeRepository).findById(validEntity.getId());
        verify(friendCodeMapper).toResponse(nonExpiredEntity);
    }

    @Test
    @DisplayName("getFriendCodeById should return null when friend code is expired")
    void getFriendCodeById_expired_shouldReturnNull() {
        // Given
        String id = validEntity.getId().toString();
        FriendCode expiredEntity = FriendCode.builder()
                .id(validEntity.getId())
                .friendCode("123456789012")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        when(friendCodeRepository.findById(validEntity.getId())).thenReturn(Optional.of(expiredEntity));

        // When
        FriendCodeResponse response = friendCodeService.getFriendCodeById(id);

        // Then
        assertThat(response).isNull();

        verify(friendCodeRepository).findById(validEntity.getId());
        verify(friendCodeMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("getFriendCodeById should return null when friend code not found")
    void getFriendCodeById_notFound_shouldReturnNull() {
        // Given
        String id = UUID.randomUUID().toString();

        when(friendCodeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When
        FriendCodeResponse response = friendCodeService.getFriendCodeById(id);

        // Then
        assertThat(response).isNull();

        verify(friendCodeRepository).findById(any(UUID.class));
        verify(friendCodeMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("getFriendCodeById should return null when invalid UUID provided")
    void getFriendCodeById_invalidUuid_shouldReturnNull() {
        // Given
        String invalidId = "invalid-uuid";

        // When
        FriendCodeResponse response = friendCodeService.getFriendCodeById(invalidId);

        // Then
        assertThat(response).isNull();

        verify(friendCodeRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("countActiveFriendCodes should return count from repository")
    void countActiveFriendCodes_shouldReturnRepositoryCount() {
        // Given
        long expectedCount = 42L;
        when(friendCodeRepository.countActiveFriendCodes(any(LocalDateTime.class))).thenReturn(expectedCount);

        // When
        long actualCount = friendCodeService.countActiveFriendCodes();

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);

        verify(friendCodeRepository).countActiveFriendCodes(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("validateSubmissionRequest should return true for valid request")
    void validateSubmissionRequest_validRequest_shouldReturnTrue() {
        // Given
        when(friendCodeMapper.toEntity(eq(validRequest), anyString())).thenReturn(validEntity);
        when(validator.validate(validEntity)).thenReturn(Collections.emptySet());

        // When
        boolean isValid = friendCodeService.validateSubmissionRequest(validRequest);

        // Then
        assertThat(isValid).isTrue();

        verify(friendCodeMapper).toEntity(eq(validRequest), anyString());
        verify(validator).validate(validEntity);
    }

    @Test
    @DisplayName("validateSubmissionRequest should return false for invalid request")
    void validateSubmissionRequest_invalidRequest_shouldReturnFalse() {
        // Given
        ConstraintViolation<FriendCode> violation = createMockViolation("Invalid data");
        when(friendCodeMapper.toEntity(eq(validRequest), anyString())).thenReturn(validEntity);
        when(validator.validate(validEntity)).thenReturn(Set.of(violation));

        // When
        boolean isValid = friendCodeService.validateSubmissionRequest(validRequest);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateSubmissionRequest should return false for null request")
    void validateSubmissionRequest_nullRequest_shouldReturnFalse() {
        // When
        boolean isValid = friendCodeService.validateSubmissionRequest(null);

        // Then
        assertThat(isValid).isFalse();

        verify(friendCodeMapper, never()).toEntity(any(), anyString());
        verify(validator, never()).validate(any());
    }

    @Test
    @DisplayName("hasExpiredCodes should return true when expired codes exist")
    void hasExpiredCodes_expiredCodesExist_shouldReturnTrue() {
        // Given
        when(friendCodeRepository.count()).thenReturn(10L);
        when(friendCodeRepository.countActiveFriendCodes(any(LocalDateTime.class))).thenReturn(7L);

        // When
        boolean hasExpired = friendCodeService.hasExpiredCodes();

        // Then
        assertThat(hasExpired).isTrue();

        verify(friendCodeRepository).count();
        verify(friendCodeRepository).countActiveFriendCodes(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("hasExpiredCodes should return false when no expired codes exist")
    void hasExpiredCodes_noExpiredCodes_shouldReturnFalse() {
        // Given
        when(friendCodeRepository.count()).thenReturn(10L);
        when(friendCodeRepository.countActiveFriendCodes(any(LocalDateTime.class))).thenReturn(10L);

        // When
        boolean hasExpired = friendCodeService.hasExpiredCodes();

        // Then
        assertThat(hasExpired).isFalse();
    }

    @SuppressWarnings("unchecked")
    private ConstraintViolation<FriendCode> createMockViolation(String message) {
        ConstraintViolation<FriendCode> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        org.mockito.Mockito.lenient().when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}
