package com.devs.simplicity.poke_go_friends.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST clients used by the Reddit API integration.
 * Provides pre-configured RestTemplate instances for Reddit OAuth and API interactions.
 */
@Configuration
public class RestClientConfig {

    @Value("${reddit.api.user-agent:java:com.devs.simplicity.poke-go-friends:v1.0.0 (by /u/YourRedditUsername)}")
    private String userAgent;

    @Value("${reddit.api.timeout:30000}")
    private int timeoutMs;

    /**
     * Creates a RestTemplate configured for Reddit OAuth token requests.
     * Used for authentication against www.reddit.com/api/v1/access_token
     *
     * @return Configured RestTemplate for Reddit OAuth
     */
    @Bean("redditOAuthRestTemplate")
    public RestTemplate redditOAuthRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(createRequestFactory());
        
        // Add User-Agent header interceptor
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", userAgent);
            request.getHeaders().set("Content-Type", "application/x-www-form-urlencoded");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }

    /**
     * Creates a RestTemplate configured for Reddit API calls with OAuth bearer token.
     * Used for authenticated requests to oauth.reddit.com
     *
     * @return Configured RestTemplate for Reddit API
     */
    @Bean("redditApiRestTemplate")
    public RestTemplate redditApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(createRequestFactory());
        
        // Add User-Agent header interceptor
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", userAgent);
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }

    /**
     * Creates a request factory with timeout configuration.
     *
     * @return Configured ClientHttpRequestFactory
     */
    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return factory;
    }
}
