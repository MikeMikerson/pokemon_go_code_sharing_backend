package com.devs.simplicity.poke_go_friends.mapper;

import com.devs.simplicity.poke_go_friends.dto.FriendCodeResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.model.FriendCode;
import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeMapper.
 * Tests mapping functionality between entities and DTOs.
 */
@DisplayName("FriendCodeMapper Tests")
class FriendCodeMapperTest {

    private FriendCodeMapper mapper;
    private String testFingerprint;

    @BeforeEach
    void setUp() {
        mapper = new FriendCodeMapper();
        testFingerprint = "test-fingerprint-123";
    }

    @Test
    @DisplayName("toEntity should convert submission request to entity with all fields")
    void toEntity_shouldConvertSubmissionRequestToEntityWithAllFields() {
        // Given
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .build();

        // When
        FriendCode entity = mapper.toEntity(request, testFingerprint);

        // Then
        assertThat(entity.getFriendCode()).isEqualTo("123456789012");
        assertThat(entity.getTrainerName()).isEqualTo("TestTrainer");
        assertThat(entity.getTrainerLevel()).isEqualTo(25);
        assertThat(entity.getTeam()).isEqualTo(Team.MYSTIC);
        assertThat(entity.getCountry()).isEqualTo("United States");
        assertThat(entity.getPurpose()).isEqualTo(Purpose.BOTH);
        assertThat(entity.getMessage()).isEqualTo("Looking for active friends!");
        assertThat(entity.getUserFingerprint()).isEqualTo(testFingerprint);
        
        // System generated fields should be null (set by @PrePersist)
        assertThat(entity.getId()).isNull();
        assertThat(entity.getSubmittedAt()).isNull();
        assertThat(entity.getExpiresAt()).isNull();
    }

    @Test
    @DisplayName("toEntity should convert submission request to entity with minimal fields")
    void toEntity_shouldConvertSubmissionRequestToEntityWithMinimalFields() {
        // Given
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .build();

        // When
        FriendCode entity = mapper.toEntity(request, testFingerprint);

        // Then
        assertThat(entity.getFriendCode()).isEqualTo("123456789012");
        assertThat(entity.getTrainerName()).isNull();
        assertThat(entity.getTrainerLevel()).isNull();
        assertThat(entity.getTeam()).isNull();
        assertThat(entity.getCountry()).isNull();
        assertThat(entity.getPurpose()).isNull();
        assertThat(entity.getMessage()).isNull();
        assertThat(entity.getUserFingerprint()).isEqualTo(testFingerprint);
    }

    @Test
    @DisplayName("toResponse should convert entity to response with all fields")
    void toResponse_shouldConvertEntityToResponseWithAllFields() {
        // Given
        LocalDateTime submittedAt = LocalDateTime.now();
        LocalDateTime expiresAt = submittedAt.plusHours(48);
        UUID id = UUID.randomUUID();

        FriendCode entity = FriendCode.builder()
                .id(id)
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Looking for active friends!")
                .submittedAt(submittedAt)
                .expiresAt(expiresAt)
                .userFingerprint(testFingerprint)
                .build();

        // When
        FriendCodeResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getFriendCode()).isEqualTo("123456789012");
        assertThat(response.getTrainerName()).isEqualTo("TestTrainer");
        assertThat(response.getTrainerLevel()).isEqualTo(25);
        assertThat(response.getTeam()).isEqualTo(Team.MYSTIC);
        assertThat(response.getCountry()).isEqualTo("United States");
        assertThat(response.getPurpose()).isEqualTo(Purpose.BOTH);
        assertThat(response.getMessage()).isEqualTo("Looking for active friends!");
        assertThat(response.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("toResponse should convert entity to response with minimal fields")
    void toResponse_shouldConvertEntityToResponseWithMinimalFields() {
        // Given
        LocalDateTime submittedAt = LocalDateTime.now();
        LocalDateTime expiresAt = submittedAt.plusHours(48);
        UUID id = UUID.randomUUID();

        FriendCode entity = FriendCode.builder()
                .id(id)
                .friendCode("123456789012")
                .submittedAt(submittedAt)
                .expiresAt(expiresAt)
                .userFingerprint(testFingerprint)
                .build();

        // When
        FriendCodeResponse response = mapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getFriendCode()).isEqualTo("123456789012");
        assertThat(response.getTrainerName()).isNull();
        assertThat(response.getTrainerLevel()).isNull();
        assertThat(response.getTeam()).isNull();
        assertThat(response.getCountry()).isNull();
        assertThat(response.getPurpose()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("toResponse should return null for null entity")
    void toResponse_shouldReturnNullForNullEntity() {
        // Given & When
        FriendCodeResponse response = mapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("toResponseList should convert list of entities to list of responses")
    void toResponseList_shouldConvertListOfEntitiesToListOfResponses() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<FriendCode> entities = List.of(
                FriendCode.builder()
                        .id(UUID.randomUUID())
                        .friendCode("123456789012")
                        .trainerName("Trainer1")
                        .submittedAt(now)
                        .expiresAt(now.plusHours(48))
                        .userFingerprint("fingerprint1")
                        .build(),
                FriendCode.builder()
                        .id(UUID.randomUUID())
                        .friendCode("987654321098")
                        .trainerName("Trainer2")
                        .submittedAt(now)
                        .expiresAt(now.plusHours(48))
                        .userFingerprint("fingerprint2")
                        .build()
        );

        // When
        List<FriendCodeResponse> responses = mapper.toResponseList(entities);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getFriendCode()).isEqualTo("123456789012");
        assertThat(responses.get(0).getTrainerName()).isEqualTo("Trainer1");
        assertThat(responses.get(1).getFriendCode()).isEqualTo("987654321098");
        assertThat(responses.get(1).getTrainerName()).isEqualTo("Trainer2");
    }

    @Test
    @DisplayName("toResponseList should return empty list for null input")
    void toResponseList_shouldReturnEmptyListForNullInput() {
        // Given & When
        List<FriendCodeResponse> responses = mapper.toResponseList(null);

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("toResponseList should handle empty list")
    void toResponseList_shouldHandleEmptyList() {
        // Given & When
        List<FriendCodeResponse> responses = mapper.toResponseList(List.of());

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("updateEntity should update existing entity with request data")
    void updateEntity_shouldUpdateExistingEntityWithRequestData() {
        // Given
        LocalDateTime originalSubmittedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime originalExpiresAt = originalSubmittedAt.plusHours(48);
        UUID id = UUID.randomUUID();

        FriendCode existingEntity = FriendCode.builder()
                .id(id)
                .friendCode("111111111111")
                .trainerName("OldTrainer")
                .trainerLevel(10)
                .team(Team.VALOR)
                .country("Canada")
                .purpose(Purpose.GIFTS)
                .message("Old message")
                .submittedAt(originalSubmittedAt)
                .expiresAt(originalExpiresAt)
                .userFingerprint("old-fingerprint")
                .build();

        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("NewTrainer")
                .trainerLevel(25)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("New message")
                .build();

        LocalDateTime beforeUpdate = LocalDateTime.now();

        // When
        FriendCode updatedEntity = mapper.updateEntity(existingEntity, request, testFingerprint);

        // Then
        LocalDateTime afterUpdate = LocalDateTime.now();

        // ID should remain the same
        assertThat(updatedEntity.getId()).isEqualTo(id);

        // All request data should be updated
        assertThat(updatedEntity.getFriendCode()).isEqualTo("123456789012");
        assertThat(updatedEntity.getTrainerName()).isEqualTo("NewTrainer");
        assertThat(updatedEntity.getTrainerLevel()).isEqualTo(25);
        assertThat(updatedEntity.getTeam()).isEqualTo(Team.MYSTIC);
        assertThat(updatedEntity.getCountry()).isEqualTo("United States");
        assertThat(updatedEntity.getPurpose()).isEqualTo(Purpose.BOTH);
        assertThat(updatedEntity.getMessage()).isEqualTo("New message");
        assertThat(updatedEntity.getUserFingerprint()).isEqualTo(testFingerprint);

        // Timestamps should be updated
        assertThat(updatedEntity.getSubmittedAt()).isNotNull();
        assertThat(updatedEntity.getSubmittedAt()).isAfter(originalSubmittedAt);
        assertThat(updatedEntity.getExpiresAt()).isEqualTo(updatedEntity.getSubmittedAt().plusHours(48));

        // Should return the same instance (modification in place)
        assertThat(updatedEntity).isSameAs(existingEntity);
    }

    @Test
    @DisplayName("All team values should be mappable")
    void allTeamValues_shouldBeMappable() {
        // Given & When & Then
        for (Team team : Team.values()) {
            FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                    .friendCode("123456789012")
                    .team(team)
                    .build();

            FriendCode entity = mapper.toEntity(request, testFingerprint);
            FriendCodeResponse response = mapper.toResponse(entity);

            assertThat(entity.getTeam()).isEqualTo(team);
            assertThat(response.getTeam()).isEqualTo(team);
        }
    }

    @Test
    @DisplayName("All purpose values should be mappable")
    void allPurposeValues_shouldBeMappable() {
        // Given & When & Then
        for (Purpose purpose : Purpose.values()) {
            FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                    .friendCode("123456789012")
                    .purpose(purpose)
                    .build();

            FriendCode entity = mapper.toEntity(request, testFingerprint);
            FriendCodeResponse response = mapper.toResponse(entity);

            assertThat(entity.getPurpose()).isEqualTo(purpose);
            assertThat(response.getPurpose()).isEqualTo(purpose);
        }
    }
}
