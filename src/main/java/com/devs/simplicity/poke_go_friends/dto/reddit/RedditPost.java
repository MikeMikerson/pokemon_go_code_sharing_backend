package com.devs.simplicity.poke_go_friends.dto.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing a Reddit post data structure.
 * Contains the essential fields from a Reddit post, focusing on title and selftext
 * which are used for friend code extraction.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditPost {
    
    private String id;
    private String title;
    private String selftext;
    private String author;
    private String subreddit;
    
    @JsonProperty("author_fullname")
    private String authorFullname;
    
    @JsonProperty("subreddit_name_prefixed")
    private String subredditNamePrefixed;
    
    private Long created;
    
    @JsonProperty("created_utc")
    private Long createdUtc;
    
    @JsonProperty("is_self")
    private Boolean isSelf;
    
    @JsonProperty("selftext_html")
    private String selftextHtml;
    
    private String permalink;
    private String url;
}
