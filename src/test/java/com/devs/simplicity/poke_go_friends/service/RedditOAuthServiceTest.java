package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RedditApiProperties;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditOAuthTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RedditOAuthService.
 * Tests OAuth token acquisition and management functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedditOAuthService Tests")
class RedditOAuthServiceTest {

    @Mock
    private RestTemplate oauthRestTemplate;

    @Mock
    private RedditApiProperties redditProperties;

    private RedditOAuthService redditOAuthService;

    @BeforeEach
    void setUp() {
        redditOAuthService = new RedditOAuthService(oauthRestTemplate, redditProperties);
        
        // Set up default lenient mock behavior
        lenient().when(redditProperties.hasValidCredentials()).thenReturn(true);
        lenient().when(redditProperties.getClientId()).thenReturn("test_client_id");
        lenient().when(redditProperties.getClientSecret()).thenReturn("test_client_secret");
        lenient().when(redditProperties.getOauthUrl()).thenReturn("https://www.reddit.com/api/v1/access_token");
        lenient().when(redditProperties.getDeviceId()).thenReturn("test_device_id");
    }

    @Nested
    @DisplayName("Token Acquisition Tests")
    class TokenAcquisitionTests {

        @Test
        @DisplayName("Should successfully obtain access token")
        void shouldSuccessfullyObtainAccessToken() {
            // Given
            RedditOAuthTokenResponse tokenResponse = createValidTokenResponse();
            ResponseEntity<RedditOAuthTokenResponse> responseEntity = 
                new ResponseEntity<>(tokenResponse, HttpStatus.OK);
            
            when(oauthRestTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RedditOAuthTokenResponse.class)
            )).thenReturn(responseEntity);

            // When
            String accessToken = redditOAuthService.getValidAccessToken();

            // Then
            assertThat(accessToken).isEqualTo("test_access_token");
        }

        @Test
        @DisplayName("Should return authorization header with bearer token")
        void shouldReturnAuthorizationHeaderWithBearerToken() {
            // Given
            RedditOAuthTokenResponse tokenResponse = createValidTokenResponse();
            ResponseEntity<RedditOAuthTokenResponse> responseEntity = 
                new ResponseEntity<>(tokenResponse, HttpStatus.OK);
            
            when(oauthRestTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RedditOAuthTokenResponse.class)
            )).thenReturn(responseEntity);

            // When
            String authHeader = redditOAuthService.getAuthorizationHeader();

            // Then
            assertThat(authHeader).isEqualTo("bearer test_access_token");
        }

        @Test
        @DisplayName("Should reuse valid token without making new request")
        void shouldReuseValidTokenWithoutMakingNewRequest() {
            // Given
            RedditOAuthTokenResponse tokenResponse = createValidTokenResponse();
            ResponseEntity<RedditOAuthTokenResponse> responseEntity = 
                new ResponseEntity<>(tokenResponse, HttpStatus.OK);
            
            when(oauthRestTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RedditOAuthTokenResponse.class)
            )).thenReturn(responseEntity);

            // When
            String firstToken = redditOAuthService.getValidAccessToken();
            String secondToken = redditOAuthService.getValidAccessToken();

            // Then
            assertThat(firstToken).isEqualTo("test_access_token");
            assertThat(secondToken).isEqualTo("test_access_token");
            assertThat(firstToken).isEqualTo(secondToken);
        }

        @Test
        @DisplayName("Should refresh token after clearing cache")
        void shouldRefreshTokenAfterClearingCache() {
            // Given
            RedditOAuthTokenResponse tokenResponse = createValidTokenResponse();
            ResponseEntity<RedditOAuthTokenResponse> responseEntity = 
                new ResponseEntity<>(tokenResponse, HttpStatus.OK);
            
            when(oauthRestTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RedditOAuthTokenResponse.class)
            )).thenReturn(responseEntity);

            // When
            String firstToken = redditOAuthService.getValidAccessToken();
            redditOAuthService.clearToken();
            String secondToken = redditOAuthService.getValidAccessToken();

            // Then
            assertThat(firstToken).isEqualTo("test_access_token");
            assertThat(secondToken).isEqualTo("test_access_token");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return null when credentials are not configured")
        void shouldReturnNullWhenCredentialsNotConfigured() {
            // Given
            when(redditProperties.hasValidCredentials()).thenReturn(false);

            // When
            String accessToken = redditOAuthService.getValidAccessToken();

            // Then
            assertThat(accessToken).isNull();
        }

        @Test
        @DisplayName("Should return null when OAuth request fails")
        void shouldReturnNullWhenOAuthRequestFails() {
            // Given
            ResponseEntity<RedditOAuthTokenResponse> responseEntity = 
                new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            
            when(oauthRestTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RedditOAuthTokenResponse.class)
            )).thenReturn(responseEntity);

            // When
            String accessToken = redditOAuthService.getValidAccessToken();

            // Then
            assertThat(accessToken).isNull();
        }

        @Test
        @DisplayName("Should return null when OAuth response is invalid")
        void shouldReturnNullWhenOAuthResponseIsInvalid() {
            // Given
            RedditOAuthTokenResponse invalidResponse = new RedditOAuthTokenResponse();
            // Don't set access token or token type - making it invalid
            ResponseEntity<RedditOAuthTokenResponse> responseEntity = 
                new ResponseEntity<>(invalidResponse, HttpStatus.OK);
            
            when(oauthRestTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RedditOAuthTokenResponse.class)
            )).thenReturn(responseEntity);

            // When
            String accessToken = redditOAuthService.getValidAccessToken();

            // Then
            assertThat(accessToken).isNull();
        }

        @Test
        @DisplayName("Should return null when OAuth request throws exception")
        void shouldReturnNullWhenOAuthRequestThrowsException() {
            // Given
            when(oauthRestTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RedditOAuthTokenResponse.class)
            )).thenThrow(new RuntimeException("Network error"));

            // When
            String accessToken = redditOAuthService.getValidAccessToken();

            // Then
            assertThat(accessToken).isNull();
        }

        @Test
        @DisplayName("Should return null authorization header when token is unavailable")
        void shouldReturnNullAuthorizationHeaderWhenTokenUnavailable() {
            // Given
            when(redditProperties.hasValidCredentials()).thenReturn(false);

            // When
            String authHeader = redditOAuthService.getAuthorizationHeader();

            // Then
            assertThat(authHeader).isNull();
        }
    }

    // Helper methods
    private RedditOAuthTokenResponse createValidTokenResponse() {
        RedditOAuthTokenResponse response = new RedditOAuthTokenResponse();
        response.setAccessToken("test_access_token");
        response.setTokenType("bearer");
        response.setExpiresIn(3600);
        response.setScope("read");
        return response;
    }
}
