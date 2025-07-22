package com.devs.simplicity.poke_go_friends.dto.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DTO representing the data section of a Reddit listing response.
 * Contains metadata about the listing and the array of children (posts).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditListingData {
    
    private String after;
    private Integer dist;
    private String modhash;
    private String geoFilter;
    private List<RedditChild> children;
}
