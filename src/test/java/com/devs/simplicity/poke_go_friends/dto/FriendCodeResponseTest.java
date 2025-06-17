package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeResponse DTO.
 * Tests DTO construction and data transfer behavior.
 */
@DisplayName("FriendCodeResponse Tests")
class FriendCodeResponseTest {

    @Test
    @DisplayName("Builder should create response with all fields")
    void builder_shouldCreateResponseWithAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime submittedAt = LocalDateTime.now();
        LocalDateTime expiresAt = submittedAt.plusHours(48);

        // When
        FriendCodeResponse response = FriendCodeResponse.builder()
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
                .build();

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
    @DisplayName("Builder should create response with minimal fields")
    void builder_shouldCreateResponseWithMinimalFields() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime submittedAt = LocalDateTime.now();
        LocalDateTime expiresAt = submittedAt.plusHours(48);

        // When
        FriendCodeResponse response = FriendCodeResponse.builder()
                .id(id)
                .friendCode("123456789012")
                .submittedAt(submittedAt)
                .expiresAt(expiresAt)
                .build();

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
    @DisplayName("Data annotation should provide equality and hashCode")
    void dataAnnotation_shouldProvideEqualityAndHashCode() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime submittedAt = LocalDateTime.now();
        LocalDateTime expiresAt = submittedAt.plusHours(48);

        FriendCodeResponse response1 = FriendCodeResponse.builder()
                .id(id)
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .submittedAt(submittedAt)
                .expiresAt(expiresAt)
                .build();

        FriendCodeResponse response2 = FriendCodeResponse.builder()
                .id(id)
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .submittedAt(submittedAt)
                .expiresAt(expiresAt)
                .build();

        // When & Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Data annotation should provide toString")
    void dataAnnotation_shouldProvideToString() {
        // Given
        FriendCodeResponse response = FriendCodeResponse.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("FriendCodeResponse");
        assertThat(toString).contains("123456789012");
        assertThat(toString).contains("TestTrainer");
    }

    @Test
    @DisplayName("All team values should be settable")
    void allTeamValues_shouldBeSettable() {
        // Given & When & Then
        for (Team team : Team.values()) {
            FriendCodeResponse response = FriendCodeResponse.builder()
                    .friendCode("123456789012")
                    .team(team)
                    .build();

            assertThat(response.getTeam()).isEqualTo(team);
        }
    }

    @Test
    @DisplayName("All purpose values should be settable")
    void allPurposeValues_shouldBeSettable() {
        // Given & When & Then
        for (Purpose purpose : Purpose.values()) {
            FriendCodeResponse response = FriendCodeResponse.builder()
                    .friendCode("123456789012")
                    .purpose(purpose)
                    .build();

            assertThat(response.getPurpose()).isEqualTo(purpose);
        }
    }
}
