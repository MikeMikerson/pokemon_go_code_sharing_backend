package com.devs.simplicity.poke_go_friends.dto.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO representing a single child element in a Reddit listing.
 * Each child represents a Reddit post with its own kind and data.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditChild {
    
    private String kind;
    private RedditPost data;
}
