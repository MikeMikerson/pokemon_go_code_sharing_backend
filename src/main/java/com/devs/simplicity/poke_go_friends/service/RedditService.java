package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.dto.reddit.RedditChild;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditListingResponse;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for fetching and parsing friend codes from Reddit.
 * Handles HTTP requests to Reddit API and extracts Pokemon Go friend codes
 * from post titles and content using regular expressions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedditService {

    @Qualifier("redditRestTemplate")
    private final RestTemplate redditRestTemplate;

    @Value("${reddit.api.url:https://www.reddit.com/r/PokemonGoFriends/new.json}")
    private String redditApiUrl;

    // Regex pattern to match 12-digit friend codes with optional spaces or hyphens
    private static final Pattern FRIEND_CODE_PATTERN = Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");

    /**
     * Fetches latest posts from r/PokemonGoFriends and extracts unique friend codes.
     * 
     * @return Set of unique 12-digit friend codes found in the posts
     */
    public Set<String> fetchFriendCodes() {
        log.info("Fetching friend codes from Reddit r/PokemonGoFriends");
        
        try {
            RedditListingResponse response = redditRestTemplate.getForObject(
                redditApiUrl, 
                RedditListingResponse.class
            );
            
            if (response == null || response.getData() == null || response.getData().getChildren() == null) {
                log.warn("Received empty or null response from Reddit API");
                return Collections.emptySet();
            }
            
            Set<String> extractedCodes = extractFriendCodesFromPosts(response.getData().getChildren());
            
            log.info("Successfully extracted {} unique friend codes from {} Reddit posts", 
                    extractedCodes.size(), response.getData().getChildren().size());
            
            return extractedCodes;
            
        } catch (Exception e) {
            log.error("Failed to fetch friend codes from Reddit: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
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
                
                // Extract from title
                if (post.getTitle() != null) {
                    friendCodes.addAll(extractFriendCodesFromText(post.getTitle()));
                }
                
                // Extract from selftext (post content)
                if (post.getSelftext() != null && !post.getSelftext().trim().isEmpty()) {
                    friendCodes.addAll(extractFriendCodesFromText(post.getSelftext()));
                }
                
                log.debug("Post '{}' by {} - extracted codes from this post", 
                        post.getTitle(), post.getAuthor());
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
            if (normalizedCode.length() == 12 && normalizedCode.matches("\\d{12}")) {
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
}
