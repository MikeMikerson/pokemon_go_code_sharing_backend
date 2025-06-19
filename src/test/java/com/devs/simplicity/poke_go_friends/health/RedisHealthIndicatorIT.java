package com.devs.simplicity.poke_go_friends.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class RedisHealthIndicatorIT {

    @Autowired
    private RedisHealthIndicator redisHealthIndicator;

    @Test
    void health_shouldProvideValidHealthStatus() {
        Health health = redisHealthIndicator.health();
        
        assertNotNull(health);
        assertNotNull(health.getStatus());
    }
}
