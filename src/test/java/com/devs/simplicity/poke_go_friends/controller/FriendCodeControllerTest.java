package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.dto.*;
import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.exception.*;
import com.devs.simplicity.poke_go_friends.service.FriendCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FriendCodeController.
 * Tests all REST endpoints with various scenarios.
 */
@WebMvcTest(value = FriendCodeController.class, 
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@DisplayName("FriendCodeController Tests")
class FriendCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FriendCodeService friendCodeService;

    private FriendCode testFriendCode;
    private FriendCodeSubmissionRequest validSubmissionRequest;
    private FriendCodeUpdateRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        testFriendCode = new FriendCode("123456789012", "TestTrainer", 25, "New York", "Looking for friends");
        testFriendCode.setId(1L);
        testFriendCode.setCreatedAt(LocalDateTime.now());
        testFriendCode.setUpdatedAt(LocalDateTime.now());

        validSubmissionRequest = new FriendCodeSubmissionRequest(
            "123456789012", "TestTrainer", 25, "New York", "Looking for friends");

        validUpdateRequest = new FriendCodeUpdateRequest(
            "UpdatedTrainer", 30, "Los Angeles", "Updated description");
    }

    @Nested
    @DisplayName("POST /api/friend-codes - Submit Friend Code")
    class SubmitFriendCodeTests {

        @Test
        @DisplayName("Should create friend code successfully")
        void shouldCreateFriendCodeSuccessfully() throws Exception {
            // Given
            when(friendCodeService.createFriendCode(anyString(), anyString(), anyInt(), 
                    anyString(), anyString(), isNull(), isNull(), anyString(), any())).thenReturn(testFriendCode);

            // When & Then
            mockMvc.perform(post("/api/friend-codes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSubmissionRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.friendCode").value("123456789012"))
                    .andExpect(jsonPath("$.trainerName").value("TestTrainer"))
                    .andExpect(jsonPath("$.playerLevel").value(25))
                    .andExpect(jsonPath("$.location").value("New York"))
                    .andExpect(jsonPath("$.description").value("Looking for friends"));

            verify(friendCodeService).createFriendCode(
                eq("123456789012"), eq("TestTrainer"), eq(25), 
                eq("New York"), eq("Looking for friends"), isNull(), isNull(), anyString(), isNull());
        }

        @Test
        @DisplayName("Should return 400 and user-friendly error for invalid team value")
        void shouldReturn400ForInvalidTeamValue() throws Exception {
            // Given: invalid team value
            String invalidTeamJson = "{" +
                    "\"friendCode\": \"123456789012\"," +
                    "\"trainerName\": \"TestTrainer\"," +
                    "\"team\": \"INVALID_TEAM\"}";

            // When & Then
            mockMvc.perform(post("/api/friend-codes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidTeamJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("team")));
        }

        @Test
        @DisplayName("Should return 400 and user-friendly error for invalid goals value")
        void shouldReturn400ForInvalidGoalsValue() throws Exception {
            // Given: invalid goals value (not a valid enum)
            String invalidGoalsJson = "{" +
                    "\"friendCode\": \"123456789012\"," +
                    "\"trainerName\": \"TestTrainer\"," +
                    "\"goals\": [\"INVALID_GOAL\"]}";

            // When & Then
            mockMvc.perform(post("/api/friend-codes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidGoalsJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("goals")));
        }

        @Test
        @DisplayName("Should return 409 for duplicate friend code")
        void shouldReturn409ForDuplicateFriendCode() throws Exception {
            // Given
            when(friendCodeService.createFriendCode(anyString(), anyString(), anyInt(), 
                    anyString(), anyString(), isNull(), isNull(), anyString(), any()))
                    .thenThrow(new DuplicateFriendCodeException("123456789012"));

            // When & Then
            mockMvc.perform(post("/api/friend-codes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSubmissionRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Duplicate Resource"));
        }
    }

    @Nested
    @DisplayName("GET /api/friend-codes - Get Friend Codes")
    class GetFriendCodesTests {

        @Test
        @DisplayName("Should return paginated friend codes")
        void shouldReturnPaginatedFriendCodes() throws Exception {
            // Given
            List<FriendCode> friendCodes = Arrays.asList(testFriendCode);
            Page<FriendCode> page = new PageImpl<>(friendCodes, PageRequest.of(0, 20), 1);
            
            when(friendCodeService.getActiveFriendCodes(any(Pageable.class))).thenReturn(page);

            // When & Then
            mockMvc.perform(get("/api/friend-codes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));

            verify(friendCodeService).getActiveFriendCodes(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/friend-codes/{id} - Get Specific Friend Code")
    class GetSpecificFriendCodeTests {

        @Test
        @DisplayName("Should return friend code by ID")
        void shouldReturnFriendCodeById() throws Exception {
            // Given
            when(friendCodeService.getFriendCodeById(1L)).thenReturn(testFriendCode);

            // When & Then
            mockMvc.perform(get("/api/friend-codes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.friendCode").value("123456789012"))
                    .andExpect(jsonPath("$.trainerName").value("TestTrainer"));

            verify(friendCodeService).getFriendCodeById(1L);
        }

        @Test
        @DisplayName("Should return 404 for non-existent friend code")
        void shouldReturn404ForNonExistentFriendCode() throws Exception {
            // Given
            when(friendCodeService.getFriendCodeById(999L))
                    .thenThrow(new FriendCodeNotFoundException(999L));

            // When & Then
            mockMvc.perform(get("/api/friend-codes/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Resource Not Found"));
        }
    }

    @Nested
    @DisplayName("PUT /api/friend-codes/{id} - Update Friend Code")
    class UpdateFriendCodeTests {

        @Test
        @DisplayName("Should update friend code successfully")
        void shouldUpdateFriendCodeSuccessfully() throws Exception {
            // Given
            FriendCode updatedFriendCode = new FriendCode("123456789012", "UpdatedTrainer", 30, "Los Angeles", "Updated description");
            updatedFriendCode.setId(1L);
            updatedFriendCode.setCreatedAt(LocalDateTime.now());
            updatedFriendCode.setUpdatedAt(LocalDateTime.now());
            
            when(friendCodeService.updateFriendCode(anyLong(), anyString(), anyInt(), 
                    anyString(), anyString(), isNull(), isNull(), any())).thenReturn(updatedFriendCode);

            // When & Then
            mockMvc.perform(put("/api/friend-codes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.trainerName").value("UpdatedTrainer"))
                    .andExpect(jsonPath("$.playerLevel").value(30))
                    .andExpect(jsonPath("$.location").value("Los Angeles"))
                    .andExpect(jsonPath("$.description").value("Updated description"));

            verify(friendCodeService).updateFriendCode(
                eq(1L), eq("UpdatedTrainer"), eq(30), eq("Los Angeles"), eq("Updated description"), isNull(), isNull(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/friend-codes/{id} - Delete Friend Code")
    class DeleteFriendCodeTests {

        @Test
        @DisplayName("Should deactivate friend code successfully")
        void shouldDeactivateFriendCodeSuccessfully() throws Exception {
            // Given
            doNothing().when(friendCodeService).deactivateFriendCode(1L, null);

            // When & Then
            mockMvc.perform(delete("/api/friend-codes/1"))
                    .andExpect(status().isNoContent());

            verify(friendCodeService).deactivateFriendCode(1L, null);
        }
    }

    @Nested
    @DisplayName("GET /api/friend-codes/stats - Get Statistics")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should return friend code statistics")
        void shouldReturnFriendCodeStatistics() throws Exception {
            // Given
            FriendCodeService.FriendCodeStats stats = new FriendCodeService.FriendCodeStats(10, 15);
            when(friendCodeService.getStatistics()).thenReturn(stats);

            // When & Then
            mockMvc.perform(get("/api/friend-codes/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activeFriendCodes").value(10))
                    .andExpect(jsonPath("$.totalFriendCodes").value(15));

            verify(friendCodeService).getStatistics();
        }
    }
}
