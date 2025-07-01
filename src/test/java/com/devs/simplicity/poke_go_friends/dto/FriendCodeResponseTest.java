package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeResponse DTO.
 * Tests data mapping and response creation behavior.
 */
@DisplayName("FriendCodeResponse")
class FriendCodeResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create response with basic constructor")
        void shouldCreateResponseWithBasicConstructor() {
            // Arrange & Act
            FriendCodeResponse response = new FriendCodeResponse(1L, "123456789012", "TestTrainer", null, null, null, null, null, null, null, null, null);

            // Assert
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFriendCode()).isEqualTo("123456789012");
            assertThat(response.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(response.getPlayerLevel()).isNull();
            assertThat(response.getLocation()).isNull();
            assertThat(response.getDescription()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
            assertThat(response.getExpiresAt()).isNull();
        }

        @Test
        @DisplayName("should create response with all args constructor")
        void shouldCreateResponseWithAllArgsConstructor() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = now.plusDays(30);

            // Act
            FriendCodeResponse response = new FriendCodeResponse(
                1L, "123456789012", "TestTrainer", 25, "New York", "Looking for gifts",
                true, now, now, expires);

            // Assert
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFriendCode()).isEqualTo("123456789012");
            assertThat(response.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(response.getPlayerLevel()).isEqualTo(25);
            assertThat(response.getLocation()).isEqualTo("New York");
            assertThat(response.getDescription()).isEqualTo("Looking for gifts");
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
            assertThat(response.getExpiresAt()).isEqualTo(expires);
        }

        @Test
        @DisplayName("should create response with no args constructor")
        void shouldCreateResponseWithNoArgsConstructor() {
            // Arrange & Act
            FriendCodeResponse response = new FriendCodeResponse();

            // Assert
            assertThat(response.getId()).isNull();
            assertThat(response.getFriendCode()).isNull();
            assertThat(response.getTrainerName()).isNull();
            assertThat(response.getPlayerLevel()).isNull();
            assertThat(response.getLocation()).isNull();
            assertThat(response.getDescription()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
            assertThat(response.getExpiresAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity Mapping Tests")
    class EntityMappingTests {

        @Test
        @DisplayName("should map from entity with all fields")
        void shouldMapFromEntityWithAllFields() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = now.plusDays(30);
            
            FriendCode entity = new FriendCode("123456789012", "TestTrainer", 25, "New York", "Looking for gifts");
            entity.setId(1L);
            entity.setIsActive(true);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entity.setExpiresAt(expires);

            // Act
            FriendCodeResponse response = FriendCodeResponse.fromEntity(entity);

            // Assert
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFriendCode()).isEqualTo("123456789012");
            assertThat(response.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(response.getPlayerLevel()).isEqualTo(25);
            assertThat(response.getLocation()).isEqualTo("New York");
            assertThat(response.getDescription()).isEqualTo("Looking for gifts");
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
            assertThat(response.getExpiresAt()).isEqualTo(expires);
        }

        @Test
        @DisplayName("should map from entity with required fields only")
        void shouldMapFromEntityWithRequiredFieldsOnly() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            
            FriendCode entity = new FriendCode("123456789012", "TestTrainer");
            entity.setId(2L);
            entity.setIsActive(true);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            // Act
            FriendCodeResponse response = FriendCodeResponse.fromEntity(entity);

            // Assert
            assertThat(response.getId()).isEqualTo(2L);
            assertThat(response.getFriendCode()).isEqualTo("123456789012");
            assertThat(response.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(response.getPlayerLevel()).isNull();
            assertThat(response.getLocation()).isNull();
            assertThat(response.getDescription()).isNull();
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
            assertThat(response.getExpiresAt()).isNull();
        }

        @Test
        @DisplayName("should map from entity with inactive status")
        void shouldMapFromEntityWithInactiveStatus() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            
            FriendCode entity = new FriendCode("123456789012", "TestTrainer");
            entity.setId(3L);
            entity.setIsActive(false);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            // Act
            FriendCodeResponse response = FriendCodeResponse.fromEntity(entity);

            // Assert
            assertThat(response.getId()).isEqualTo(3L);
            assertThat(response.getFriendCode()).isEqualTo("123456789012");
            assertThat(response.getTrainerName()).isEqualTo("TestTrainer");
            assertThat(response.getIsActive()).isFalse();
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("should preserve all data when mapping from entity")
        void shouldPreserveAllDataWhenMappingFromEntity() {
            // Arrange
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 12, 0);
            LocalDateTime expiresAt = LocalDateTime.of(2023, 2, 1, 12, 0);
            
            FriendCode entity = new FriendCode("987654321098", "AnotherTrainer", 40, "Tokyo", "XP farming");
            entity.setId(100L);
            entity.setIsActive(true);
            entity.setCreatedAt(createdAt);
            entity.setUpdatedAt(updatedAt);
            entity.setExpiresAt(expiresAt);

            // Act
            FriendCodeResponse response = FriendCodeResponse.fromEntity(entity);

            // Assert - Verify all fields are correctly mapped
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getFriendCode()).isEqualTo("987654321098");
            assertThat(response.getTrainerName()).isEqualTo("AnotherTrainer");
            assertThat(response.getPlayerLevel()).isEqualTo(40);
            assertThat(response.getLocation()).isEqualTo("Tokyo");
            assertThat(response.getDescription()).isEqualTo("XP farming");
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getCreatedAt()).isEqualTo(createdAt);
            assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("should handle edge case values correctly")
        void shouldHandleEdgeCaseValuesCorrectly() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            
            FriendCode entity = new FriendCode("000000000000", "AB", 1, "", "");
            entity.setId(0L);
            entity.setIsActive(false);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            // Act
            FriendCodeResponse response = FriendCodeResponse.fromEntity(entity);

            // Assert
            assertThat(response.getId()).isEqualTo(0L);
            assertThat(response.getFriendCode()).isEqualTo("000000000000");
            assertThat(response.getTrainerName()).isEqualTo("AB");
            assertThat(response.getPlayerLevel()).isEqualTo(1);
            assertThat(response.getLocation()).isEqualTo("");
            assertThat(response.getDescription()).isEqualTo("");
            assertThat(response.getIsActive()).isFalse();
        }
    }
}
