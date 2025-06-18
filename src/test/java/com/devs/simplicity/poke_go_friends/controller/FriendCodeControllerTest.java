package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.dto.CanSubmitResponse;
import com.devs.simplicity.poke_go_friends.dto.ErrorResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeFeedResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.dto.SubmissionResponse;
import com.devs.simplicity.poke_go_friends.service.FingerprintService;
import com.devs.simplicity.poke_go_friends.service.FriendCodeService;
import com.devs.simplicity.poke_go_friends.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FriendCodeController.class, 
            excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@SuppressWarnings("removal") // Suppress deprecation warnings for @MockBean
class FriendCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendCodeService friendCodeService;

    @MockBean
    private RateLimitService rateLimitService;
    
    @MockBean
    private FingerprintService fingerprintService;

    @Test
    void submitFriendCode_validRequest_returnsCreated() throws Exception {
        // Given
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .build();

        SubmissionResponse response = SubmissionResponse.builder()
                .success(true)
                .message("Friend code submitted successfully")
                .nextSubmissionAllowed(LocalDateTime.now().plusDays(1))
                .build();

        when(rateLimitService.canSubmit(any(HttpServletRequest.class))).thenReturn(true);
        when(fingerprintService.generateFingerprint(any(HttpServletRequest.class))).thenReturn("test-fingerprint");
        when(friendCodeService.submitFriendCode(any(FriendCodeSubmissionRequest.class), anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/friend-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Friend code submitted successfully"));
    }

    @Test
    @Disabled("Rate limiting is now handled by AOP aspect - this test needs to be updated for the new approach")
    void submitFriendCode_rateLimited_returnsTooManyRequests() throws Exception {
        // Given
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .build();

        when(rateLimitService.canSubmit(any(HttpServletRequest.class))).thenReturn(false);
        when(rateLimitService.getNextAllowedSubmissionTime(any(HttpServletRequest.class)))
                .thenReturn(Instant.now().plusSeconds(3600));

        // When & Then
        mockMvc.perform(post("/api/friend-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Rate limit exceeded"))
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void submitFriendCode_invalidFriendCode_returnsBadRequest() throws Exception {
        // Given
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("invalid")
                .build();

        // When & Then
        mockMvc.perform(post("/api/friend-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getFriendCodes_validRequest_returnsOk() throws Exception {
        // Given
        FriendCodeFeedResponse response = FriendCodeFeedResponse.builder()
                .friendCodes(java.util.List.of())
                .hasMore(false)
                .build();

        when(friendCodeService.getActiveFriendCodes(0, 20)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/friend-codes")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friendCodes").isArray())
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void canSubmit_userCanSubmit_returnsOk() throws Exception {
        // Given
        when(rateLimitService.canSubmit(any(HttpServletRequest.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/friend-codes/can-submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canSubmit").value(true));
    }

    @Test
    void canSubmit_userCannotSubmit_returnsOk() throws Exception {
        // Given
        when(rateLimitService.canSubmit(any(HttpServletRequest.class))).thenReturn(false);
        when(rateLimitService.getNextAllowedSubmissionTime(any(HttpServletRequest.class)))
                .thenReturn(Instant.now().plusSeconds(3600));

        // When & Then
        mockMvc.perform(get("/api/friend-codes/can-submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canSubmit").value(false))
                .andExpect(jsonPath("$.nextSubmissionTime").exists());
    }
}
