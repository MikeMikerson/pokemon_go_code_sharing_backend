package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.config.SecurityConfig;
import com.devs.simplicity.poke_go_friends.controller.GlobalExceptionHandler;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeFeedResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.dto.SubmissionResponse;
import com.devs.simplicity.poke_go_friends.service.FingerprintService;
import com.devs.simplicity.poke_go_friends.service.FriendCodeService;
import com.devs.simplicity.poke_go_friends.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FriendCodeControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private FriendCodeService friendCodeService;

    @Mock
    private RateLimitService rateLimitService;
    
    @Mock
    private FingerprintService fingerprintService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        FriendCodeController controller = new FriendCodeController(
                friendCodeService, rateLimitService, fingerprintService);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void submitFriendCode_validRequest_returnsCreated() throws Exception {
        // Given
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .trainerLevel(11)
                .build();

        SubmissionResponse response = SubmissionResponse.builder()
                .success(true)
                .message("Friend code submitted successfully")
                .nextSubmissionAllowed(LocalDateTime.now().plusDays(1))
                .build();

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
    void submitFriendCode_invalidFriendCode_returnsBadRequest() throws Exception {
        // Given
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("invalid")
                .build();

        // When & Then
        mockMvc.perform(post("/api/friend-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFriendCodes_validRequest_returnsOk() throws Exception {
        // Given
        FriendCodeFeedResponse response = FriendCodeFeedResponse.builder()
                .friendCodes(List.of())
                .hasMore(false)
                .build();

        when(friendCodeService.getActiveFriendCodes(anyInt(), anyInt())).thenReturn(response);

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
        when(rateLimitService.getNextAllowedSubmissionTime(any(HttpServletRequest.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/friend-codes/can-submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canSubmit").value(true))
                .andExpect(jsonPath("$.nextSubmissionTime").doesNotExist());
    }

    @Test
    void canSubmit_userCannotSubmit_returnsOk() throws Exception {
        // Given
        when(rateLimitService.canSubmit(any(HttpServletRequest.class))).thenReturn(false);
        Instant nextSubmissionTime = Instant.now().plusSeconds(3600);
        when(rateLimitService.getNextAllowedSubmissionTime(any(HttpServletRequest.class)))
                .thenReturn(nextSubmissionTime);

        // When & Then
        mockMvc.perform(get("/api/friend-codes/can-submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canSubmit").value(false))
                .andExpect(jsonPath("$.nextSubmissionTime").exists());
    }
}
