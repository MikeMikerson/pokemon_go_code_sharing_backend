package com.devs.simplicity.poke_go_friends.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FriendCodeFeedResponse DTO.
 * Tests factory methods and pagination behavior.
 */
@DisplayName("FriendCodeFeedResponse Tests")
class FriendCodeFeedResponseTest {

    @Test
    @DisplayName("of factory method should create feed response with pagination")
    void of_shouldCreateFeedResponseWithPagination() {
        // Given
        List<FriendCodeResponse> friendCodes = createTestFriendCodes(10);
        boolean hasMore = true;
        String nextCursor = "cursor123";
        int totalElements = 25;
        int currentPage = 0;
        int pageSize = 10;

        // When
        FriendCodeFeedResponse response = FriendCodeFeedResponse.of(
                friendCodes, hasMore, nextCursor, totalElements, currentPage, pageSize);

        // Then
        assertThat(response.getFriendCodes()).isEqualTo(friendCodes);
        assertThat(response.isHasMore()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(nextCursor);
        assertThat(response.getTotalElements()).isEqualTo(totalElements);
        assertThat(response.getCurrentPage()).isEqualTo(currentPage);
        assertThat(response.getPageSize()).isEqualTo(pageSize);
    }

    @Test
    @DisplayName("of factory method should create feed response for last page")
    void of_shouldCreateFeedResponseForLastPage() {
        // Given
        List<FriendCodeResponse> friendCodes = createTestFriendCodes(5);
        boolean hasMore = false;
        String nextCursor = null;
        int totalElements = 15;
        int currentPage = 2;
        int pageSize = 10;

        // When
        FriendCodeFeedResponse response = FriendCodeFeedResponse.of(
                friendCodes, hasMore, nextCursor, totalElements, currentPage, pageSize);

        // Then
        assertThat(response.getFriendCodes()).isEqualTo(friendCodes);
        assertThat(response.isHasMore()).isFalse();
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.getTotalElements()).isEqualTo(totalElements);
        assertThat(response.getCurrentPage()).isEqualTo(currentPage);
        assertThat(response.getPageSize()).isEqualTo(pageSize);
    }

    @Test
    @DisplayName("of factory method should handle empty result set")
    void of_shouldHandleEmptyResultSet() {
        // Given
        List<FriendCodeResponse> friendCodes = List.of();
        boolean hasMore = false;
        String nextCursor = null;
        int totalElements = 0;
        int currentPage = 0;
        int pageSize = 10;

        // When
        FriendCodeFeedResponse response = FriendCodeFeedResponse.of(
                friendCodes, hasMore, nextCursor, totalElements, currentPage, pageSize);

        // Then
        assertThat(response.getFriendCodes()).isEmpty();
        assertThat(response.isHasMore()).isFalse();
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getCurrentPage()).isZero();
        assertThat(response.getPageSize()).isEqualTo(pageSize);
    }

    @Test
    @DisplayName("Builder should create response with all fields")
    void builder_shouldCreateResponseWithAllFields() {
        // Given
        List<FriendCodeResponse> friendCodes = createTestFriendCodes(3);
        boolean hasMore = true;
        String nextCursor = "test-cursor";
        int totalElements = 50;
        int currentPage = 1;
        int pageSize = 20;

        // When
        FriendCodeFeedResponse response = FriendCodeFeedResponse.builder()
                .friendCodes(friendCodes)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .totalElements(totalElements)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .build();

        // Then
        assertThat(response.getFriendCodes()).isEqualTo(friendCodes);
        assertThat(response.isHasMore()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(nextCursor);
        assertThat(response.getTotalElements()).isEqualTo(totalElements);
        assertThat(response.getCurrentPage()).isEqualTo(currentPage);
        assertThat(response.getPageSize()).isEqualTo(pageSize);
    }

    @Test
    @DisplayName("Data annotation should provide equality and hashCode")
    void dataAnnotation_shouldProvideEqualityAndHashCode() {
        // Given
        List<FriendCodeResponse> friendCodes = createTestFriendCodes(2);

        FriendCodeFeedResponse response1 = FriendCodeFeedResponse.builder()
                .friendCodes(friendCodes)
                .hasMore(true)
                .nextCursor("cursor")
                .totalElements(10)
                .currentPage(0)
                .pageSize(5)
                .build();

        FriendCodeFeedResponse response2 = FriendCodeFeedResponse.builder()
                .friendCodes(friendCodes)
                .hasMore(true)
                .nextCursor("cursor")
                .totalElements(10)
                .currentPage(0)
                .pageSize(5)
                .build();

        // When & Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Data annotation should provide toString")
    void dataAnnotation_shouldProvideToString() {
        // Given
        FriendCodeFeedResponse response = FriendCodeFeedResponse.builder()
                .friendCodes(List.of())
                .hasMore(false)
                .totalElements(0)
                .build();

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("FriendCodeFeedResponse");
        assertThat(toString).contains("hasMore=false");
        assertThat(toString).contains("totalElements=0");
    }

    @Test
    @DisplayName("Builder should create minimal response")
    void builder_shouldCreateMinimalResponse() {
        // Given & When
        FriendCodeFeedResponse response = FriendCodeFeedResponse.builder()
                .friendCodes(List.of())
                .hasMore(false)
                .totalElements(0)
                .currentPage(0)
                .pageSize(10)
                .build();

        // Then
        assertThat(response.getFriendCodes()).isEmpty();
        assertThat(response.isHasMore()).isFalse();
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getCurrentPage()).isZero();
        assertThat(response.getPageSize()).isEqualTo(10);
    }

    private List<FriendCodeResponse> createTestFriendCodes(int count) {
        List<FriendCodeResponse> friendCodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            friendCodes.add(FriendCodeResponse.builder()
                    .id(UUID.randomUUID())
                    .friendCode(String.format("12345678901%d", i))
                    .build());
        }
        return friendCodes;
    }
}
