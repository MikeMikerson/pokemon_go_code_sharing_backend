package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.mapper.FriendCodeMapper;
import com.devs.simplicity.poke_go_friends.model.FriendCode;
import com.devs.simplicity.poke_go_friends.model.Purpose;
import com.devs.simplicity.poke_go_friends.model.Team;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DTO classes.
 * Tests end-to-end validation, mapping, and JSON serialization.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("DTO Integration Tests")
class DtoIntegrationTest {

    private Validator validator;
    private ObjectMapper objectMapper;
    private FriendCodeMapper mapper;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mapper = new FriendCodeMapper();
    }

    @Test
    @DisplayName("Complete flow from request to response should work correctly")
    void completeFlow_fromRequestToResponse_shouldWorkCorrectly() throws Exception {
        // Given - Create a valid submission request
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("IntegrationTestTrainer")
                .trainerLevel(30)
                .team(Team.MYSTIC)
                .country("United States")
                .purpose(Purpose.BOTH)
                .message("Integration test message")
                .build();

        // Validate the request
        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();

        // When - Convert to entity
        FriendCode entity = mapper.toEntity(request, "integration-test-fingerprint");
        
        // Simulate @PrePersist behavior manually
        LocalDateTime now = LocalDateTime.now();
        entity.setSubmittedAt(now);
        entity.setExpiresAt(now.plusHours(48));
        entity.setId(UUID.randomUUID());

        // Convert back to response
        FriendCodeResponse response = mapper.toResponse(entity);

        // Then - Verify the mapping
        assertThat(response.getId()).isNotNull();
        assertThat(response.getFriendCode()).isEqualTo(request.getFriendCode());
        assertThat(response.getTrainerName()).isEqualTo(request.getTrainerName());
        assertThat(response.getTrainerLevel()).isEqualTo(request.getTrainerLevel());
        assertThat(response.getTeam()).isEqualTo(request.getTeam());
        assertThat(response.getCountry()).isEqualTo(request.getCountry());
        assertThat(response.getPurpose()).isEqualTo(request.getPurpose());
        assertThat(response.getMessage()).isEqualTo(request.getMessage());
        assertThat(response.getSubmittedAt()).isNotNull();
        assertThat(response.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("JSON serialization and deserialization should work for all DTOs")
    void jsonSerialization_shouldWorkForAllDtos() throws Exception {
        // Given - Create test data
        LocalDateTime now = LocalDateTime.now();
        FriendCodeResponse friendCode = FriendCodeResponse.builder()
                .id(UUID.randomUUID())
                .friendCode("123456789012")
                .trainerName("JsonTestTrainer")
                .trainerLevel(25)
                .team(Team.VALOR)
                .country("Canada")
                .purpose(Purpose.RAIDS)
                .message("JSON test message")
                .submittedAt(now)
                .expiresAt(now.plusHours(48))
                .build();

        // Test FriendCodeResponse serialization
        String friendCodeJson = objectMapper.writeValueAsString(friendCode);
        FriendCodeResponse deserializedFriendCode = objectMapper.readValue(friendCodeJson, FriendCodeResponse.class);
        assertThat(deserializedFriendCode).isEqualTo(friendCode);

        // Test SubmissionResponse serialization
        SubmissionResponse submissionResponse = SubmissionResponse.success(friendCode, now.plusHours(24));
        String submissionJson = objectMapper.writeValueAsString(submissionResponse);
        SubmissionResponse deserializedSubmission = objectMapper.readValue(submissionJson, SubmissionResponse.class);
        assertThat(deserializedSubmission.isSuccess()).isTrue();
        assertThat(deserializedSubmission.getFriendCode().getId()).isEqualTo(friendCode.getId());

        // Test FriendCodeFeedResponse serialization
        FriendCodeFeedResponse feedResponse = FriendCodeFeedResponse.of(
                List.of(friendCode), true, "cursor123", 10, 0, 5);
        String feedJson = objectMapper.writeValueAsString(feedResponse);
        FriendCodeFeedResponse deserializedFeed = objectMapper.readValue(feedJson, FriendCodeFeedResponse.class);
        assertThat(deserializedFeed.getFriendCodes()).hasSize(1);
        assertThat(deserializedFeed.isHasMore()).isTrue();

        // Test ErrorResponse serialization
        ErrorResponse errorResponse = ErrorResponse.badRequest("Test error", "/api/test");
        String errorJson = objectMapper.writeValueAsString(errorResponse);
        ErrorResponse deserializedError = objectMapper.readValue(errorJson, ErrorResponse.class);
        assertThat(deserializedError.getStatus()).isEqualTo(400);
        assertThat(deserializedError.getMessage()).isEqualTo("Test error");
    }

    @Test
    @DisplayName("Validation should work correctly across all DTOs")
    void validation_shouldWorkCorrectlyAcrossAllDtos() {
        // Test FriendCodeSubmissionRequest validation
        FriendCodeSubmissionRequest invalidRequest = FriendCodeSubmissionRequest.builder()
                .friendCode("invalid") // Invalid format
                .trainerName("a".repeat(51)) // Too long
                .trainerLevel(51) // Too high
                .country("Invalid Country") // Invalid country
                .message("a".repeat(101)) // Too long
                .build();

        Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(invalidRequest);
        assertThat(violations).hasSize(5); // All fields should have violations

        // Extract violation messages
        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(violationMessages).containsExactlyInAnyOrder(
                "Friend code must be exactly 12 digits",
                "Trainer name cannot exceed 50 characters",
                "Trainer level cannot exceed 50",
                "Invalid country code",
                "Message cannot exceed 100 characters"
        );
    }

    @Test
    @DisplayName("All enum values should be supported in DTOs")
    void allEnumValues_shouldBeSupportedInDtos() {
        // Test all Team values
        for (Team team : Team.values()) {
            FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                    .friendCode("123456789012")
                    .team(team)
                    .build();

            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();

            FriendCode entity = mapper.toEntity(request, "test-fingerprint");
            LocalDateTime now = LocalDateTime.now();
            entity.setSubmittedAt(now);
            entity.setExpiresAt(now.plusHours(48));
            entity.setId(UUID.randomUUID());

            FriendCodeResponse response = mapper.toResponse(entity);
            assertThat(response.getTeam()).isEqualTo(team);
        }

        // Test all Purpose values
        for (Purpose purpose : Purpose.values()) {
            FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                    .friendCode("123456789012")
                    .purpose(purpose)
                    .build();

            Set<ConstraintViolation<FriendCodeSubmissionRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();

            FriendCode entity = mapper.toEntity(request, "test-fingerprint");
            LocalDateTime now = LocalDateTime.now();
            entity.setSubmittedAt(now);
            entity.setExpiresAt(now.plusHours(48));
            entity.setId(UUID.randomUUID());

            FriendCodeResponse response = mapper.toResponse(entity);
            assertThat(response.getPurpose()).isEqualTo(purpose);
        }
    }

    @Test
    @DisplayName("Error response factory methods should create valid responses")
    void errorResponseFactoryMethods_shouldCreateValidResponses() {
        String testPath = "/api/friend-codes";

        // Test all factory methods
        ErrorResponse rateLimitError = ErrorResponse.rateLimitExceeded(testPath);
        assertThat(rateLimitError.getStatus()).isEqualTo(429);
        assertThat(rateLimitError.getPath()).isEqualTo(testPath);

        ErrorResponse badRequestError = ErrorResponse.badRequest("Invalid input", testPath);
        assertThat(badRequestError.getStatus()).isEqualTo(400);
        assertThat(badRequestError.getMessage()).isEqualTo("Invalid input");

        ErrorResponse serverError = ErrorResponse.internalServerError(testPath);
        assertThat(serverError.getStatus()).isEqualTo(500);
        assertThat(serverError.getPath()).isEqualTo(testPath);

        ErrorResponse validationError = ErrorResponse.validationError(
                400, "Validation Failed", "Multiple errors", testPath, 
                List.of("Error 1", "Error 2"));
        assertThat(validationError.getDetails()).hasSize(2);
    }

    @Test
    @DisplayName("Feed response pagination should work correctly")
    void feedResponsePagination_shouldWorkCorrectly() {
        // Create test friend codes
        List<FriendCodeResponse> friendCodes = List.of(
                FriendCodeResponse.builder()
                        .id(UUID.randomUUID())
                        .friendCode("123456789012")
                        .build(),
                FriendCodeResponse.builder()
                        .id(UUID.randomUUID())
                        .friendCode("987654321098")
                        .build()
        );

        // Test first page
        FriendCodeFeedResponse firstPage = FriendCodeFeedResponse.of(
                friendCodes, true, "cursor123", 10, 0, 2);

        assertThat(firstPage.getFriendCodes()).hasSize(2);
        assertThat(firstPage.isHasMore()).isTrue();
        assertThat(firstPage.getCurrentPage()).isZero();
        assertThat(firstPage.getTotalElements()).isEqualTo(10);

        // Test last page
        FriendCodeFeedResponse lastPage = FriendCodeFeedResponse.of(
                List.of(friendCodes.get(0)), false, null, 3, 1, 2);

        assertThat(lastPage.getFriendCodes()).hasSize(1);
        assertThat(lastPage.isHasMore()).isFalse();
        assertThat(lastPage.getNextCursor()).isNull();
        assertThat(lastPage.getCurrentPage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Country validation should work in complete flow")
    void countryValidation_shouldWorkInCompleteFlow() {
        // Test valid country
        FriendCodeSubmissionRequest validRequest = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .country("United States")
                .build();

        Set<ConstraintViolation<FriendCodeSubmissionRequest>> validViolations = validator.validate(validRequest);
        assertThat(validViolations).isEmpty();

        // Test invalid country
        FriendCodeSubmissionRequest invalidRequest = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .country("NonExistentCountry")
                .build();

        Set<ConstraintViolation<FriendCodeSubmissionRequest>> invalidViolations = validator.validate(invalidRequest);
        assertThat(invalidViolations).hasSize(1);
        assertThat(invalidViolations.iterator().next().getMessage()).isEqualTo("Invalid country code");

        // Test null country (should be valid)
        FriendCodeSubmissionRequest nullCountryRequest = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .country(null)
                .build();

        Set<ConstraintViolation<FriendCodeSubmissionRequest>> nullViolations = validator.validate(nullCountryRequest);
        assertThat(nullViolations).isEmpty();
    }
}
