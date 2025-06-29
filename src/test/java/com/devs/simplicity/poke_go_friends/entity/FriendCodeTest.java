package com.devs.simplicity.poke_go_friends.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FriendCodeTest {

    private FriendCode friendCode;

    @BeforeEach
    void setUp() {
        friendCode = new FriendCode();
    }

    @Test
    void createFriendCode_withValidData_shouldSetAllFields() {
        // Given
        String code = "123456789012";
        String trainerName = "TestTrainer";
        Integer playerLevel = 25;
        String location = "New York, USA";
        String description = "Looking for daily gift exchange";
        LocalDateTime now = LocalDateTime.now();

        // When
        friendCode.setFriendCode(code);
        friendCode.setTrainerName(trainerName);
        friendCode.setPlayerLevel(playerLevel);
        friendCode.setLocation(location);
        friendCode.setDescription(description);
        friendCode.setIsActive(true);
        friendCode.setCreatedAt(now);
        friendCode.setUpdatedAt(now);

        // Then
        assertThat(friendCode.getFriendCode()).isEqualTo(code);
        assertThat(friendCode.getTrainerName()).isEqualTo(trainerName);
        assertThat(friendCode.getPlayerLevel()).isEqualTo(playerLevel);
        assertThat(friendCode.getLocation()).isEqualTo(location);
        assertThat(friendCode.getDescription()).isEqualTo(description);
        assertThat(friendCode.getIsActive()).isTrue();
        assertThat(friendCode.getCreatedAt()).isEqualTo(now);
        assertThat(friendCode.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void createFriendCode_withMinimalData_shouldSetRequiredFields() {
        // Given
        String code = "987654321098";
        String trainerName = "MinimalTrainer";

        // When
        friendCode.setFriendCode(code);
        friendCode.setTrainerName(trainerName);

        // Then
        assertThat(friendCode.getFriendCode()).isEqualTo(code);
        assertThat(friendCode.getTrainerName()).isEqualTo(trainerName);
        assertThat(friendCode.getPlayerLevel()).isNull();
        assertThat(friendCode.getLocation()).isNull();
        assertThat(friendCode.getDescription()).isNull();
    }

    @Test
    void createFriendCode_defaultConstructor_shouldInitializeDefaults() {
        // When
        FriendCode newFriendCode = new FriendCode();

        // Then
        assertThat(newFriendCode.getId()).isNull();
        assertThat(newFriendCode.getIsActive()).isTrue(); // Set to true by default
        assertThat(newFriendCode.getCreatedAt()).isNull(); // Will be auto-generated in JPA
        assertThat(newFriendCode.getUpdatedAt()).isNull(); // Will be auto-generated in JPA
    }

    @Test
    void setExpiresAt_withFutureDate_shouldSetExpiration() {
        // Given
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);

        // When
        friendCode.setExpiresAt(futureDate);

        // Then
        assertThat(friendCode.getExpiresAt()).isEqualTo(futureDate);
    }

    @Test
    void friendCodeEquality_withSameId_shouldBeEqual() {
        // Given
        FriendCode friendCode1 = new FriendCode();
        FriendCode friendCode2 = new FriendCode();
        friendCode1.setId(1L);
        friendCode2.setId(1L);

        // When & Then
        assertThat(friendCode1).isEqualTo(friendCode2);
        assertThat(friendCode1.hashCode()).isEqualTo(friendCode2.hashCode());
    }

    @Test
    void friendCodeEquality_withDifferentId_shouldNotBeEqual() {
        // Given
        FriendCode friendCode1 = new FriendCode();
        FriendCode friendCode2 = new FriendCode();
        friendCode1.setId(1L);
        friendCode2.setId(2L);

        // When & Then
        assertThat(friendCode1).isNotEqualTo(friendCode2);
    }

    @Test
    void toString_shouldContainKeyFields() {
        // Given
        friendCode.setId(1L);
        friendCode.setFriendCode("123456789012");
        friendCode.setTrainerName("TestTrainer");

        // When
        String result = friendCode.toString();

        // Then
        assertThat(result).contains("1");
        assertThat(result).contains("123456789012");
        assertThat(result).contains("TestTrainer");
    }

    @Test
    void constructor_withRequiredFields_shouldSetFieldsCorrectly() {
        // Given
        String code = "123456789012";
        String trainerName = "TestTrainer";

        // When
        FriendCode friendCode = new FriendCode(code, trainerName);

        // Then
        assertThat(friendCode.getFriendCode()).isEqualTo(code);
        assertThat(friendCode.getTrainerName()).isEqualTo(trainerName);
        assertThat(friendCode.getIsActive()).isTrue();
    }

    @Test
    void constructor_withAllFields_shouldSetAllFieldsCorrectly() {
        // Given
        String code = "987654321098";
        String trainerName = "FullTrainer";
        Integer level = 40;
        String location = "Tokyo, Japan";
        String description = "Looking for raid partners";

        // When
        FriendCode friendCode = new FriendCode(code, trainerName, level, location, description);

        // Then
        assertThat(friendCode.getFriendCode()).isEqualTo(code);
        assertThat(friendCode.getTrainerName()).isEqualTo(trainerName);
        assertThat(friendCode.getPlayerLevel()).isEqualTo(level);
        assertThat(friendCode.getLocation()).isEqualTo(location);
        assertThat(friendCode.getDescription()).isEqualTo(description);
        assertThat(friendCode.getIsActive()).isTrue();
    }

    @Test
    void isCurrentlyActive_withActiveStatusAndNoExpiration_shouldReturnTrue() {
        // Given
        friendCode.setIsActive(true);
        friendCode.setExpiresAt(null);

        // When
        boolean result = friendCode.isCurrentlyActive();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isCurrentlyActive_withInactiveStatus_shouldReturnFalse() {
        // Given
        friendCode.setIsActive(false);

        // When
        boolean result = friendCode.isCurrentlyActive();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isCurrentlyActive_withActiveStatusAndFutureExpiration_shouldReturnTrue() {
        // Given
        friendCode.setIsActive(true);
        friendCode.setExpiresAt(LocalDateTime.now().plusDays(1));

        // When
        boolean result = friendCode.isCurrentlyActive();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isCurrentlyActive_withActiveStatusAndPastExpiration_shouldReturnFalse() {
        // Given
        friendCode.setIsActive(true);
        friendCode.setExpiresAt(LocalDateTime.now().minusDays(1));

        // When
        boolean result = friendCode.isCurrentlyActive();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void deactivate_shouldSetIsActiveToFalse() {
        // Given
        friendCode.setIsActive(true);

        // When
        friendCode.deactivate();

        // Then
        assertThat(friendCode.getIsActive()).isFalse();
    }

    @Test
    void setExpiration_shouldSetExpiresAt() {
        // Given
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

        // When
        friendCode.setExpiration(futureDate);

        // Then
        assertThat(friendCode.getExpiresAt()).isEqualTo(futureDate);
    }
}
