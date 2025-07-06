package com.devs.simplicity.poke_go_friends.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to verify that LocalDateTime fields are properly serialized with UTC timezone information.
 * This addresses the issue where datetime fields were being serialized without timezone info,
 * causing confusion on the frontend.
 */
@JsonTest
@DisplayName("DateTime Serialization Tests")
class DateTimeSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("should serialize FriendCodeResponse with UTC timezone format")
    void shouldSerializeFriendCodeResponseWithUtcTimezoneFormat() throws JsonProcessingException {
        // Arrange
        LocalDateTime testTime = LocalDateTime.of(2025, 7, 6, 10, 30, 45, 123000000);
        FriendCodeResponse response = new FriendCodeResponse();
        response.setId(1L);
        response.setFriendCode("123456789012");
        response.setTrainerName("TestTrainer");
        response.setCreatedAt(testTime);
        response.setUpdatedAt(testTime);

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertThat(json).contains("\"createdAt\":\"2025-07-06T10:30:45.123Z\"");
        assertThat(json).contains("\"updatedAt\":\"2025-07-06T10:30:45.123Z\"");
        assertThat(json).doesNotContain("\"createdAt\":\"2025-07-06T10:30:45.123\""); // Should NOT be without timezone
    }

    @Test
    @DisplayName("should serialize ErrorResponse with UTC timezone format")
    void shouldSerializeErrorResponseWithUtcTimezoneFormat() throws JsonProcessingException {
        // Arrange
        LocalDateTime testTime = LocalDateTime.of(2025, 7, 6, 15, 45, 30, 500000000);
        ErrorResponse response = new ErrorResponse();
        response.setStatus(400);
        response.setError("Bad Request");
        response.setMessage("Test error");
        response.setTimestamp(testTime);

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertThat(json).contains("\"timestamp\":\"2025-07-06T15:45:30.500Z\"");
        assertThat(json).doesNotContain("\"timestamp\":\"2025-07-06T15:45:30.500\""); // Should NOT be without timezone
    }

    @Test
    @DisplayName("should deserialize FriendCodeResponse with UTC timezone format")
    void shouldDeserializeFriendCodeResponseWithUtcTimezoneFormat() throws JsonProcessingException {
        // Arrange
        String json = """
            {
                "id": 1,
                "friendCode": "123456789012",
                "trainerName": "TestTrainer",
                "createdAt": "2025-07-06T10:30:45.123Z",
                "updatedAt": "2025-07-06T10:30:45.123Z"
            }
            """;

        // Act
        FriendCodeResponse response = objectMapper.readValue(json, FriendCodeResponse.class);

        // Assert
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 7, 6, 10, 30, 45, 123000000));
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 7, 6, 10, 30, 45, 123000000));
    }
}
