package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.service.FriendCodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Team parameter handling in friend code endpoints.
 */
@WebMvcTest(value = FriendCodeController.class)
@ActiveProfiles("test")
@DisplayName("Team Parameter Integration Tests")
class TeamParameterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendCodeService friendCodeService;

    @Test
    @DisplayName("Should accept valid lowercase team parameter")
    void shouldAcceptValidLowercaseTeamParameter() throws Exception {
        // Given
        when(friendCodeService.searchWithCriteria(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/friend-codes")
                        .param("team", "valor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should accept valid uppercase team parameter")
    void shouldAcceptValidUppercaseTeamParameter() throws Exception {
        // Given
        when(friendCodeService.searchWithCriteria(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/friend-codes")
                        .param("team", "MYSTIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should accept valid mixed case team parameter")
    void shouldAcceptValidMixedCaseTeamParameter() throws Exception {
        // Given
        when(friendCodeService.searchWithCriteria(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/friend-codes")
                        .param("team", "InStInCt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return 400 for invalid team parameter")
    void shouldReturn400ForInvalidTeamParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/friend-codes")
                        .param("team", "invalid_team"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid value 'invalid_team' for parameter 'team'. Valid values are: mystic, valor, instinct"));
    }

    @Test
    @DisplayName("Should work without team parameter")
    void shouldWorkWithoutTeamParameter() throws Exception {
        // Given
        when(friendCodeService.getActiveFriendCodes(any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/friend-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}