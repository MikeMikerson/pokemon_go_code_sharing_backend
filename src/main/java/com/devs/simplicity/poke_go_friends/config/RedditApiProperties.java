package com.devs.simplicity.poke_go_friends.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Reddit API integration.
 * Maps application properties with the reddit.api prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "reddit.api")
@Data
public class RedditApiProperties {

    /**
     * Reddit OAuth2 client ID.
     * Obtained from https://www.reddit.com/prefs/apps
     */
    private String clientId;

    /**
     * Reddit OAuth2 client secret.
     * Obtained from https://www.reddit.com/prefs/apps
     */
    private String clientSecret;

    /**
     * User agent string for API requests.
     * Format: platform:app-id:version (by /u/username)
     */
    private String userAgent = "java:com.devs.simplicity.poke-go-friends:v1.0.0 (by /u/YourRedditUsername)";

    /**
     * Base URL for Reddit OAuth token requests.
     */
    private String oauthUrl = "https://www.reddit.com/api/v1/access_token";

    /**
     * Base URL for Reddit API requests (with OAuth).
     */
    private String apiUrl = "https://oauth.reddit.com";

    /**
     * Subreddit to fetch posts from.
     */
    private String subreddit = "PokemonGoFriends";

    /**
     * Sort order for subreddit posts (new, hot, top, etc.).
     */
    private String sortOrder = "new";

    /**
     * Limit for number of posts to fetch per request.
     */
    private Integer limit = 25;

    /**
     * Request timeout in milliseconds.
     */
    private Integer timeoutMs = 30000;

    /**
     * Device ID for application-only OAuth (for installed apps).
     * Should be a unique 20-30 character string per installation.
     */
    private String deviceId = "poke-go-friends-backend-app";

    /**
     * Gets the full subreddit API endpoint URL.
     *
     * @return Full URL for subreddit posts endpoint
     */
    public String getSubredditUrl() {
        return String.format("%s/r/%s/%s.json", apiUrl, subreddit, sortOrder);
    }

    /**
     * Validates that required OAuth credentials are configured.
     *
     * @return true if client ID and secret are present
     */
    public boolean hasValidCredentials() {
        return clientId != null && !clientId.isEmpty() && 
               clientSecret != null && !clientSecret.isEmpty();
    }
}
