package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RedditApiProperties;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditChild;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditListingResponse;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for fetching and parsing friend codes from Reddit.
 * Handles OAuth authentication and HTTP requests to Reddit API, then extracts 
 * Pokemon Go friend codes from post titles and content using regular expressions.
 */
@Service
@Slf4j
public class RedditService {

    private final RestTemplate redditApiRestTemplate;
    
    private final RedditOAuthService redditOAuthService;
    private final RedditApiProperties redditProperties;

    /**
     * Constructor with dependency injection.
     * 
     * @param redditApiRestTemplate RestTemplate configured for Reddit API requests
     * @param redditOAuthService Service for managing Reddit OAuth authentication
     * @param redditProperties Reddit API configuration properties
     */
    public RedditService(@Qualifier("redditApiRestTemplate") RestTemplate redditApiRestTemplate,
                         RedditOAuthService redditOAuthService,
                         RedditApiProperties redditProperties) {
        this.redditApiRestTemplate = redditApiRestTemplate;
        this.redditOAuthService = redditOAuthService;
        this.redditProperties = redditProperties;
    }

    // Regex pattern to match 12-digit friend codes with optional spaces or hyphens
    private static final Pattern FRIEND_CODE_PATTERN = Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");

    /**
     * Fetches latest posts from r/PokemonGoFriends and extracts unique friend codes.
     * Uses Reddit's official OAuth API for authenticated access.
     * 
     * @return Set of unique 12-digit friend codes found in the posts
     */
    public Set<String> fetchFriendCodes() {
        log.info("Fetching friend codes from Reddit r/{}", redditProperties.getSubreddit());
        
        try {
            String authHeader = redditOAuthService.getAuthorizationHeader();
            if (authHeader == null) {
                log.error("Failed to obtain Reddit access token");
                return Collections.emptySet();
            }

            String url = buildSubredditUrl();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            log.debug("Making authenticated request to: {}", url);
            
            ResponseEntity<RedditListingResponse> response = redditApiRestTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                RedditListingResponse.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Reddit API returned status: {}", response.getStatusCode());
                return Collections.emptySet();
            }
            
            RedditListingResponse listingResponse = response.getBody();
            if (listingResponse == null || listingResponse.getData() == null || listingResponse.getData().getChildren() == null) {
                log.warn("Received empty or null response from Reddit API");
                return Collections.emptySet();
            }
            
            Set<String> extractedCodes = extractFriendCodesFromPosts(listingResponse.getData().getChildren());
            
            log.info("Successfully extracted {} unique friend codes from {} Reddit posts", 
                    extractedCodes.size(), listingResponse.getData().getChildren().size());
            
            return extractedCodes;
            
        } catch (Exception e) {
            log.error("Failed to fetch friend codes from Reddit: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * Builds the complete URL for fetching subreddit posts with query parameters.
     *
     * @return Complete subreddit URL with parameters
     */
    private String buildSubredditUrl() {
        return UriComponentsBuilder.fromHttpUrl(redditProperties.getSubredditUrl())
                .queryParam("limit", redditProperties.getLimit())
                .queryParam("raw_json", "1") // Prevents HTML entity encoding
                .build()
                .toUriString();
    }

    /**
     * Extracts friend codes from a list of Reddit posts.
     * 
     * @param children List of Reddit post children
     * @return Set of unique friend codes
     */
    private Set<String> extractFriendCodesFromPosts(List<RedditChild> children) {
        Set<String> friendCodes = new HashSet<>();
        
        for (RedditChild child : children) {
            if (child.getData() != null) {
                RedditPost post = child.getData();
                int initialSize = friendCodes.size();
                
                // Extract from title
                if (post.getTitle() != null) {
                    Set<String> titleCodes = extractFriendCodesFromText(post.getTitle());
                    friendCodes.addAll(titleCodes);
                }
                
                // Extract from selftext (post content)
                if (post.getSelftext() != null && !post.getSelftext().trim().isEmpty()) {
                    Set<String> contentCodes = extractFriendCodesFromText(post.getSelftext());
                    friendCodes.addAll(contentCodes);
                }
                
                int codesFromThisPost = friendCodes.size() - initialSize;
                log.debug("Post '{}' by {} - extracted {} codes from this post", 
                        post.getTitle(), post.getAuthor(), codesFromThisPost);
            }
        }
        
        return friendCodes;
    }

    /**
     * Extracts friend codes from a single text string using regex pattern.
     * 
     * @param text Text to search for friend codes
     * @return Set of unique friend codes found in the text
     */
    private Set<String> extractFriendCodesFromText(String text) {
        Set<String> codes = new HashSet<>();
        
        if (text == null || text.trim().isEmpty()) {
            return codes;
        }
        
        Matcher matcher = FRIEND_CODE_PATTERN.matcher(text);
        while (matcher.find()) {
            String code = matcher.group();
            // Normalize the code by removing spaces and hyphens
            String normalizedCode = code.replaceAll("[\\s-]", "");
            
            // Validate it's exactly 12 digits
            if (isValidFriendCode(normalizedCode)) {
                codes.add(normalizedCode);
                log.debug("Found friend code: {} (normalized: {})", code, normalizedCode);
            }
        }
        
        return codes;
    }

    /**
     * Validates a friend code string to ensure it's exactly 12 digits.
     * 
     * @param friendCode The friend code to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidFriendCode(String friendCode) {
        return friendCode != null && 
               friendCode.length() == 12 && 
               friendCode.matches("\\d{12}");
    }

    /**
     * Clears the OAuth token cache, forcing re-authentication on next request.
     * Useful for testing or troubleshooting authentication issues.
     */
    public void clearAuthenticationCache() {
        redditOAuthService.clearToken();
        log.info("Reddit authentication cache cleared");
    }
}
