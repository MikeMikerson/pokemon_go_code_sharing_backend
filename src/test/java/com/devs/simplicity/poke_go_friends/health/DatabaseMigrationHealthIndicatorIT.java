package com.devs.simplicity.poke_go_friends.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseMigrationHealthIndicatorIT {

    @Autowired(required = false)
    private DatabaseMigrationHealthIndicator healthIndicator;

    @Test
    void health_shouldProvideValidHealthStatus() {
        if (healthIndicator != null) {
            Health health = healthIndicator.health();
            
            assertNotNull(health);
            assertNotNull(health.getStatus());
            assertTrue(health.getDetails().containsKey("migrations"));
        }
    }
}
