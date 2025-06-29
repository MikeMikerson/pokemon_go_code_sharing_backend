package com.devs.simplicity.poke_go_friends.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for HealthController.
 * Tests all health check endpoints.
 */
@WebMvcTest(value = HealthController.class, 
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@DisplayName("HealthController Tests")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataSource dataSource;

    @Nested
    @DisplayName("GET /api/health - Health Check")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return healthy status when database is available")
        void shouldReturnHealthyStatusWhenDatabaseIsAvailable() throws Exception {
            // Given
            Connection mockConnection = mock(Connection.class);
            when(dataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.isValid(anyInt())).thenReturn(true);
            when(mockConnection.getMetaData()).thenReturn(mock(java.sql.DatabaseMetaData.class));
            when(mockConnection.getMetaData().getDatabaseProductName()).thenReturn("H2");
            when(mockConnection.getMetaData().getURL()).thenReturn("jdbc:h2:mem:test");

            // When & Then
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.application").value("Pokemon Go Friend Code Sharing API"))
                    .andExpect(jsonPath("$.database.status").value("UP"));

            verify(dataSource).getConnection();
            verify(mockConnection).isValid(5);
        }

        @Test
        @DisplayName("Should return unhealthy status when database is unavailable")
        void shouldReturnUnhealthyStatusWhenDatabaseIsUnavailable() throws Exception {
            // Given
            when(dataSource.getConnection()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value("DOWN"))
                    .andExpect(jsonPath("$.database.status").value("DOWN"));

            verify(dataSource).getConnection();
        }
    }

    @Nested
    @DisplayName("GET /api/health/ready - Readiness Check")
    class ReadinessCheckTests {

        @Test
        @DisplayName("Should return ready when database is connected")
        void shouldReturnReadyWhenDatabaseIsConnected() throws Exception {
            // Given
            Connection mockConnection = mock(Connection.class);
            when(dataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.isValid(anyInt())).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/health/ready"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("READY"));

            verify(dataSource).getConnection();
            verify(mockConnection).isValid(2);
        }

        @Test
        @DisplayName("Should return not ready when database is not connected")
        void shouldReturnNotReadyWhenDatabaseIsNotConnected() throws Exception {
            // Given
            when(dataSource.getConnection()).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/health/ready"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value("NOT_READY"))
                    .andExpect(jsonPath("$.reason").exists());

            verify(dataSource).getConnection();
        }
    }

    @Nested
    @DisplayName("GET /api/health/live - Liveness Check")
    class LivenessCheckTests {

        @Test
        @DisplayName("Should always return alive status")
        void shouldAlwaysReturnAliveStatus() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/health/live"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ALIVE"))
                    .andExpect(jsonPath("$.timestamp").exists());

            // Verify no database interaction for liveness check
            verifyNoInteractions(dataSource);
        }
    }
}
