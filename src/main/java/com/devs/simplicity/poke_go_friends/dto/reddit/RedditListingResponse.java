package com.devs.simplicity.poke_go_friends.dto.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DTO representing the main Reddit API response structure.
 * Maps to the root response object containing kind and data fields.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditListingResponse {
    
    private String kind;
    private RedditListingData data;
}
