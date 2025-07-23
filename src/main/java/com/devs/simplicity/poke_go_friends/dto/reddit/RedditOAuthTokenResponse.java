package com.devs.simplicity.poke_go_friends.dto.reddit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO representing the response from Reddit's OAuth2 token endpoint.
 * Used when requesting access tokens for API authentication.
 */
@Data
public class RedditOAuthTokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private Integer expiresIn;
    
    @JsonProperty("scope")
    private String scope;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    /**
     * Checks if this token response is valid and contains required fields.
     *
     * @return true if access token and token type are present
     */
    public boolean isValid() {
        return accessToken != null && !accessToken.isEmpty() && 
               tokenType != null && !tokenType.isEmpty();
    }
    
    /**
     * Gets the authorization header value for API requests.
     *
     * @return Authorization header value in format "bearer {token}"
     */
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }
}
