package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeFeedResponse DTO.
 * Tests pagination data mapping and response creation behavior.
 */
@DisplayName("FriendCodeFeedResponse")
class FriendCodeFeedResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create response with all args constructor")
        void shouldCreateResponseWithAllArgsConstructor() {
            // Arrange
            List<FriendCodeResponse> content = Arrays.asList(
                new FriendCodeResponse(1L, "123456789012", "Trainer1"),
                new FriendCodeResponse(2L, "234567890123", "Trainer2")
            );

            // Act
            FriendCodeFeedResponse response = new FriendCodeFeedResponse(
                content, 0, 20, 2L, 1, true, true, false);

            // Assert
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
            assertThat(response.getTotalElements()).isEqualTo(2L);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("should create response with simple constructor")
        void shouldCreateResponseWithSimpleConstructor() {
            // Arrange
            List<FriendCodeResponse> content = Arrays.asList(
                new FriendCodeResponse(1L, "123456789012", "Trainer1")
            );

            // Act
            FriendCodeFeedResponse response = new FriendCodeFeedResponse(content);

            // Assert
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1L);
            assertThat(response.isEmpty()).isFalse();
            // Other fields should have default values
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(0);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isFirst()).isFalse();
            assertThat(response.isLast()).isFalse();
        }

        @Test
        @DisplayName("should create response with empty content using simple constructor")
        void shouldCreateResponseWithEmptyContentUsingSimpleConstructor() {
            // Arrange
            List<FriendCodeResponse> content = Collections.emptyList();

            // Act
            FriendCodeFeedResponse response = new FriendCodeFeedResponse(content);

            // Assert
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0L);
            assertThat(response.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should create response with no args constructor")
        void shouldCreateResponseWithNoArgsConstructor() {
            // Arrange & Act
            FriendCodeFeedResponse response = new FriendCodeFeedResponse();

            // Assert
            assertThat(response.getContent()).isNull();
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(0);
            assertThat(response.getTotalElements()).isEqualTo(0L);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isFirst()).isFalse();
            assertThat(response.isLast()).isFalse();
            assertThat(response.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Page Mapping Tests")
    class PageMappingTests {

        @Test
        @DisplayName("should map from Spring Data Page with content")
        void shouldMapFromSpringDataPageWithContent() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            
            List<FriendCode> entities = Arrays.asList(
                createFriendCodeEntity(1L, "123456789012", "Trainer1", 25, now),
                createFriendCodeEntity(2L, "234567890123", "Trainer2", 30, now),
                createFriendCodeEntity(3L, "345678901234", "Trainer3", 35, now)
            );
            
            Pageable pageable = PageRequest.of(0, 20);
            Page<FriendCode> page = new PageImpl<>(entities, pageable, 3);

            // Act
            FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(page);

            // Assert
            assertThat(response.getContent()).hasSize(3);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
            assertThat(response.getTotalElements()).isEqualTo(3L);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isEmpty()).isFalse();

            // Verify content mapping
            FriendCodeResponse firstResponse = response.getContent().get(0);
            assertThat(firstResponse.getId()).isEqualTo(1L);
            assertThat(firstResponse.getFriendCode()).isEqualTo("123456789012");
            assertThat(firstResponse.getTrainerName()).isEqualTo("Trainer1");
            assertThat(firstResponse.getPlayerLevel()).isEqualTo(25);
        }

        @Test
        @DisplayName("should map from empty Spring Data Page")
        void shouldMapFromEmptySpringDataPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<FriendCode> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

            // Act
            FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(page);

            // Assert
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
            assertThat(response.getTotalElements()).isEqualTo(0L);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should map from Spring Data Page with pagination")
        void shouldMapFromSpringDataPageWithPagination() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            
            List<FriendCode> entities = Arrays.asList(
                createFriendCodeEntity(11L, "111111111111", "Trainer11", 25, now),
                createFriendCodeEntity(12L, "222222222222", "Trainer12", 30, now)
            );
            
            Pageable pageable = PageRequest.of(1, 10); // Second page, 10 items per page
            Page<FriendCode> page = new PageImpl<>(entities, pageable, 25); // 25 total items

            // Act
            FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(page);

            // Assert
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getPage()).isEqualTo(1); // Second page (0-indexed)
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotalElements()).isEqualTo(25L);
            assertThat(response.getTotalPages()).isEqualTo(3); // 25 items / 10 per page = 3 pages
            assertThat(response.isFirst()).isFalse(); // Not first page
            assertThat(response.isLast()).isFalse(); // Not last page (there's page 2)
            assertThat(response.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("should map from Spring Data Page on last page")
        void shouldMapFromSpringDataPageOnLastPage() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            
            List<FriendCode> entities = Arrays.asList(
                createFriendCodeEntity(21L, "333333333333", "Trainer21", 40, now)
            );
            
            Pageable pageable = PageRequest.of(2, 10); // Third page, 10 items per page
            Page<FriendCode> page = new PageImpl<>(entities, pageable, 21); // 21 total items

            // Act
            FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(page);

            // Assert
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getPage()).isEqualTo(2); // Third page (0-indexed)
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotalElements()).isEqualTo(21L);
            assertThat(response.getTotalPages()).isEqualTo(3); // 21 items / 10 per page = 3 pages
            assertThat(response.isFirst()).isFalse(); // Not first page
            assertThat(response.isLast()).isTrue(); // Is last page
            assertThat(response.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("should preserve all entity data when mapping from page")
        void shouldPreserveAllEntityDataWhenMappingFromPage() {
            // Arrange
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 12, 0);
            LocalDateTime expiresAt = LocalDateTime.of(2023, 2, 1, 12, 0);
            
            FriendCode entity = new FriendCode("987654321098", "DetailedTrainer", 45, "London", "Raid battles");
            entity.setId(99L);
            entity.setIsActive(true);
            entity.setCreatedAt(createdAt);
            entity.setUpdatedAt(updatedAt);
            entity.setExpiresAt(expiresAt);
            
            List<FriendCode> entities = Arrays.asList(entity);
            Pageable pageable = PageRequest.of(0, 1);
            Page<FriendCode> page = new PageImpl<>(entities, pageable, 1);

            // Act
            FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(page);

            // Assert
            assertThat(response.getContent()).hasSize(1);
            
            FriendCodeResponse friendCodeResponse = response.getContent().get(0);
            assertThat(friendCodeResponse.getId()).isEqualTo(99L);
            assertThat(friendCodeResponse.getFriendCode()).isEqualTo("987654321098");
            assertThat(friendCodeResponse.getTrainerName()).isEqualTo("DetailedTrainer");
            assertThat(friendCodeResponse.getPlayerLevel()).isEqualTo(45);
            assertThat(friendCodeResponse.getLocation()).isEqualTo("London");
            assertThat(friendCodeResponse.getDescription()).isEqualTo("Raid battles");
            assertThat(friendCodeResponse.getIsActive()).isTrue();
            assertThat(friendCodeResponse.getCreatedAt()).isEqualTo(createdAt);
            assertThat(friendCodeResponse.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(friendCodeResponse.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("should handle mixed active and inactive friend codes")
        void shouldHandleMixedActiveAndInactiveFriendCodes() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            
            List<FriendCode> entities = Arrays.asList(
                createActiveFriendCodeEntity(1L, "111111111111", "ActiveTrainer", now),
                createInactiveFriendCodeEntity(2L, "222222222222", "InactiveTrainer", now)
            );
            
            Pageable pageable = PageRequest.of(0, 10);
            Page<FriendCode> page = new PageImpl<>(entities, pageable, 2);

            // Act
            FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(page);

            // Assert
            assertThat(response.getContent()).hasSize(2);
            
            FriendCodeResponse activeResponse = response.getContent().get(0);
            assertThat(activeResponse.getIsActive()).isTrue();
            assertThat(activeResponse.getTrainerName()).isEqualTo("ActiveTrainer");
            
            FriendCodeResponse inactiveResponse = response.getContent().get(1);
            assertThat(inactiveResponse.getIsActive()).isFalse();
            assertThat(inactiveResponse.getTrainerName()).isEqualTo("InactiveTrainer");
        }
    }

    // Helper methods
    private FriendCode createFriendCodeEntity(Long id, String friendCode, String trainerName, Integer level, LocalDateTime time) {
        FriendCode entity = new FriendCode(friendCode, trainerName, level, null, null);
        entity.setId(id);
        entity.setIsActive(true);
        entity.setCreatedAt(time);
        entity.setUpdatedAt(time);
        return entity;
    }

    private FriendCode createActiveFriendCodeEntity(Long id, String friendCode, String trainerName, LocalDateTime time) {
        FriendCode entity = new FriendCode(friendCode, trainerName);
        entity.setId(id);
        entity.setIsActive(true);
        entity.setCreatedAt(time);
        entity.setUpdatedAt(time);
        return entity;
    }

    private FriendCode createInactiveFriendCodeEntity(Long id, String friendCode, String trainerName, LocalDateTime time) {
        FriendCode entity = new FriendCode(friendCode, trainerName);
        entity.setId(id);
        entity.setIsActive(false);
        entity.setCreatedAt(time);
        entity.setUpdatedAt(time);
        return entity;
    }
}
