package com.devs.simplicity.poke_go_friends.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for the application.
 * 
 * This configuration sets up the RedisTemplate with appropriate serializers
 * for use with the rate limiting system.
 */
@Configuration
public class RedisConfig {
    
    /**
     * Configures RedisTemplate with String serializers for both keys and values.
     * 
     * This is important for the rate limiting implementation as it uses
     * string keys and the Lua script expects string-serialized data.
     * 
     * @param connectionFactory The Redis connection factory
     * @return Configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializers for both keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        // Enable transaction support
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        return template;
    }
}
