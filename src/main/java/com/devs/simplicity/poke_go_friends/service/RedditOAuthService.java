package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RedditApiProperties;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditOAuthTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service responsible for managing Reddit OAuth2 authentication.
 * Handles token acquisition, refresh, and validation for Reddit API access.
 */
@Service
@Slf4j
public class RedditOAuthService {

    private final RestTemplate oauthRestTemplate;
    
    private final RedditApiProperties redditProperties;
    
    private volatile RedditOAuthTokenResponse currentToken;
    private volatile Instant tokenExpiryTime;
    private final ReentrantLock tokenLock = new ReentrantLock();

    /**
     * Constructor with dependency injection.
     * 
     * @param oauthRestTemplate RestTemplate configured for Reddit OAuth requests
     * @param redditProperties Reddit API configuration properties
     */
    public RedditOAuthService(@Qualifier("redditOAuthRestTemplate") RestTemplate oauthRestTemplate,
                              RedditApiProperties redditProperties) {
        this.oauthRestTemplate = oauthRestTemplate;
        this.redditProperties = redditProperties;
    }

    /**
     * Gets a valid access token for Reddit API requests.
     * Automatically handles token refresh if the current token is expired.
     *
     * @return Valid access token, or null if authentication fails
     */
    public String getValidAccessToken() {
        if (isTokenValid()) {
            return currentToken.getAccessToken();
        }

        tokenLock.lock();
        try {
            // Double-check after acquiring lock
            if (isTokenValid()) {
                return currentToken.getAccessToken();
            }

            return refreshAccessToken();
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Gets the authorization header value for API requests.
     *
     * @return Authorization header value in format "bearer {token}", or null if no token
     */
    public String getAuthorizationHeader() {
        String token = getValidAccessToken();
        return token != null ? "bearer " + token : null;
    }

    /**
     * Checks if the current token is valid and not expired.
     *
     * @return true if token is valid and not expired
     */
    private boolean isTokenValid() {
        return currentToken != null && 
               currentToken.isValid() && 
               tokenExpiryTime != null && 
               Instant.now().isBefore(tokenExpiryTime.minusSeconds(60)); // 60 second buffer
    }

    /**
     * Requests a new access token from Reddit's OAuth endpoint.
     * Uses application-only OAuth (client credentials grant).
     *
     * @return Access token if successful, null otherwise
     */
    private String refreshAccessToken() {
        if (!redditProperties.hasValidCredentials()) {
            log.error("Reddit API credentials not configured. Please set reddit.api.client-id and reddit.api.client-secret");
            return null;
        }

        try {
            log.info("Requesting new Reddit access token");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", createBasicAuthHeader());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "https://oauth.reddit.com/grants/installed_client");
            body.add("device_id", redditProperties.getDeviceId());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<RedditOAuthTokenResponse> response = oauthRestTemplate.postForEntity(
                redditProperties.getOauthUrl(),
                request,
                RedditOAuthTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                currentToken = response.getBody();
                
                if (currentToken.isValid()) {
                    // Set expiry time (tokens expire in 1 hour)
                    int expiresIn = currentToken.getExpiresIn() != null ? currentToken.getExpiresIn() : 3600;
                    tokenExpiryTime = Instant.now().plusSeconds(expiresIn);
                    
                    log.info("Successfully obtained Reddit access token, expires in {} seconds", expiresIn);
                    return currentToken.getAccessToken();
                } else {
                    log.error("Received invalid token response from Reddit OAuth");
                }
            } else {
                log.error("Failed to obtain Reddit access token, status: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Exception while requesting Reddit access token: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * Creates HTTP Basic Authentication header for OAuth requests.
     *
     * @return Basic authentication header value
     */
    private String createBasicAuthHeader() {
        String credentials = redditProperties.getClientId() + ":" + redditProperties.getClientSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /**
     * Clears the current token, forcing a refresh on next request.
     * Useful for testing or manual token refresh.
     */
    public void clearToken() {
        tokenLock.lock();
        try {
            currentToken = null;
            tokenExpiryTime = null;
            log.info("Reddit access token cleared");
        } finally {
            tokenLock.unlock();
        }
    }
}
