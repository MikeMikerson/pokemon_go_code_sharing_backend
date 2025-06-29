package com.devs.simplicity.poke_go_friends.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller providing application and database health status.
 * Provides simple health endpoints for monitoring and load balancer checks.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController implements HealthIndicator {

    private final DataSource dataSource;

    /**
     * Basic health check endpoint.
     * GET /api/health
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        log.debug("Health check requested");
        
        Map<String, Object> healthInfo = new HashMap<>();
        
        try {
            // Check application status
            healthInfo.put("status", "UP");
            healthInfo.put("timestamp", java.time.LocalDateTime.now());
            healthInfo.put("application", "Pokemon Go Friend Code Sharing API");
            healthInfo.put("version", "1.0.0");
            
            // Check database connectivity
            Map<String, Object> databaseInfo = checkDatabaseHealth();
            healthInfo.put("database", databaseInfo);
            
            // Determine overall status
            boolean isHealthy = "UP".equals(databaseInfo.get("status"));
            
            if (!isHealthy) {
                healthInfo.put("status", "DOWN");
                return ResponseEntity.status(503).body(healthInfo);
            }
            
            log.debug("Health check completed successfully");
            return ResponseEntity.ok(healthInfo);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            
            return ResponseEntity.status(503).body(healthInfo);
        }
    }

    /**
     * Simple readiness check for load balancers.
     * GET /api/health/ready
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        log.debug("Readiness check requested");
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Quick database connectivity check
            boolean isReady = isDatabaseConnected();
            
            if (isReady) {
                response.put("status", "READY");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "NOT_READY");
                response.put("reason", "Database not available");
                return ResponseEntity.status(503).body(response);
            }
            
        } catch (Exception e) {
            log.warn("Readiness check failed", e);
            
            response.put("status", "NOT_READY");
            response.put("reason", e.getMessage());
            
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Simple liveness check for Kubernetes/Docker.
     * GET /api/health/live
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        log.debug("Liveness check requested");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "ALIVE");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Implementation of Spring Boot Actuator HealthIndicator.
     */
    @Override
    public Health health() {
        try {
            Map<String, Object> databaseInfo = checkDatabaseHealth();
            boolean isHealthy = "UP".equals(databaseInfo.get("status"));
            
            if (isHealthy) {
                return Health.up()
                    .withDetail("database", databaseInfo)
                    .withDetail("application", "Pokemon Go Friend Code Sharing API")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", databaseInfo)
                    .build();
            }
            
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }

    /**
     * Check database connectivity and return detailed status.
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> databaseInfo = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Test database connection
            boolean isValid = connection.isValid(5); // 5 second timeout
            
            if (isValid) {
                databaseInfo.put("status", "UP");
                databaseInfo.put("database", connection.getMetaData().getDatabaseProductName());
                databaseInfo.put("url", connection.getMetaData().getURL());
                databaseInfo.put("validationQuery", "SELECT 1");
            } else {
                databaseInfo.put("status", "DOWN");
                databaseInfo.put("error", "Connection validation failed");
            }
            
        } catch (Exception e) {
            log.error("Database health check failed", e);
            databaseInfo.put("status", "DOWN");
            databaseInfo.put("error", e.getMessage());
        }
        
        return databaseInfo;
    }

    /**
     * Quick database connectivity check for readiness probe.
     */
    private boolean isDatabaseConnected() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // 2 second timeout for quick check
        } catch (Exception e) {
            log.debug("Database connectivity check failed: {}", e.getMessage());
            return false;
        }
    }
}
