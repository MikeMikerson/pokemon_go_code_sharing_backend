package com.devs.simplicity.poke_go_friends.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RedisHealthIndicatorTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @InjectMocks
    private RedisHealthIndicator redisHealthIndicator;

    @Test
    void health_whenRedisIsUp_shouldReturnUp() {
        RedisConnection connection = mock(RedisConnection.class);
        Properties info = new Properties();
        info.setProperty("redis_version", "6.2.5");
        when(redisConnectionFactory.getConnection()).thenReturn(connection);
        when(connection.info()).thenReturn(info);

        Health health = redisHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("6.2.5", health.getDetails().get("version"));
    }

    @Test
    void health_whenRedisIsDown_shouldReturnDown() {
        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Connection failed"));

        Health health = redisHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Connection failed", health.getDetails().get("error"));
    }
}
