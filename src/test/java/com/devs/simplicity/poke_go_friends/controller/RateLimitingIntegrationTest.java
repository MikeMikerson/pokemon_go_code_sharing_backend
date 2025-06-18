package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Rate Limiting Integration Test")
@Disabled("Integration test temporarily disabled until Redis is properly configured for testing")
class RateLimitingIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("should apply rate limiting to friend code submissions")
    void submitFriendCode_rateLimitingEnabled_shouldEnforceRateLimit() throws Exception {
        // Arrange
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        // Act & Assert
        // First request should be successful (or fail for other reasons, but not rate limiting)
        mockMvc.perform(post("/api/friend-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Test-Agent")
                .content(requestJson))
                .andExpect(status().is(not(429))); // Should not be rate limited
        
        // Second request with same fingerprint should be rate limited
        mockMvc.perform(post("/api/friend-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Test-Agent")
                .content(requestJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"));
    }
    
    @Test
    @DisplayName("should allow requests from different fingerprints")
    void submitFriendCode_differentFingerprints_shouldAllowSeparateRequests() throws Exception {
        // Arrange
        FriendCodeSubmissionRequest request = FriendCodeSubmissionRequest.builder()
                .friendCode("123456789012")
                .trainerName("TestTrainer")
                .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        
        // Act & Assert
        // Request from first user agent
        mockMvc.perform(post("/api/friend-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Test-Agent-1")
                .header("X-Forwarded-For", "192.168.1.1")
                .content(requestJson))
                .andExpect(status().is(not(429)));
        
        // Request from different user agent and IP should not be rate limited
        mockMvc.perform(post("/api/friend-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Test-Agent-2")
                .header("X-Forwarded-For", "192.168.1.2")
                .content(requestJson))
                .andExpect(status().is(not(429)));
    }
}
