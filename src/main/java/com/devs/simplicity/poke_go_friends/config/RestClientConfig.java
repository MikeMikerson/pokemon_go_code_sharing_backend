package com.devs.simplicity.poke_go_friends.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST clients used by the Reddit scraper.
 * Provides a pre-configured RestTemplate for Reddit API interactions.
 */
@Configuration
public class RestClientConfig {

    @Value("${reddit.api.user-agent:Pokemon-Go-Friends-Bot/1.0}")
    private String userAgent;

    /**
     * Creates a RestTemplate configured for Reddit API calls.
     * Includes appropriate headers and timeout configurations.
     *
     * @return Configured RestTemplate for Reddit API
     */
    @Bean("redditRestTemplate")
    public RestTemplate redditRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add User-Agent header interceptor
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", userAgent);
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
}
