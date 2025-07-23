package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.config.RedditApiProperties;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditChild;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditListingData;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditListingResponse;
import com.devs.simplicity.poke_go_friends.dto.reddit.RedditPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RedditService.
 * Tests friend code extraction logic and Reddit API integration with OAuth.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedditService Tests")
class RedditServiceTest {

    @Mock
    private RestTemplate redditApiRestTemplate;

    @Mock
    private RedditOAuthService redditOAuthService;

    @Mock
    private RedditApiProperties redditProperties;

    private RedditService redditService;

    @BeforeEach
    void setUp() {
        redditService = new RedditService(redditApiRestTemplate, redditOAuthService, redditProperties);
        
        // Set up default lenient mock behavior
        lenient().when(redditProperties.getSubreddit()).thenReturn("PokemonGoFriends");
        lenient().when(redditProperties.getSubredditUrl()).thenReturn("https://oauth.reddit.com/r/PokemonGoFriends/new.json");
        lenient().when(redditProperties.getLimit()).thenReturn(25);
        lenient().when(redditOAuthService.getAuthorizationHeader()).thenReturn("bearer mock_token");
    }

    @Nested
    @DisplayName("Friend Code Extraction Tests")
    class FriendCodeExtractionTests {

        @Test
        @DisplayName("Should extract friend codes from post titles")
        void shouldExtractFriendCodesFromPostTitles() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Add me for daily gifts 123456789012", "")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).containsExactly("123456789012");
        }

        @Test
        @DisplayName("Should extract friend codes from post selftext")
        void shouldExtractFriendCodesFromPostSelftext() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Looking for friends", "My friend code is 987654321098")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).containsExactly("987654321098");
        }

        @Test
        @DisplayName("Should extract friend codes with spaces")
        void shouldExtractFriendCodesWithSpaces() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Add me 1234 5678 9012", "")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).containsExactly("123456789012");
        }

        @Test
        @DisplayName("Should extract friend codes with hyphens")
        void shouldExtractFriendCodesWithHyphens() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Friend code: 1234-5678-9012", "")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).containsExactly("123456789012");
        }

        @Test
        @DisplayName("Should extract multiple unique friend codes from multiple posts")
        void shouldExtractMultipleUniqueFriendCodes() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Add me 123456789012", ""),
                createMockPost("Looking for friends", "987654321098"),
                createMockPost("Daily gifts", "456789012345")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).containsExactlyInAnyOrder(
                "123456789012", 
                "987654321098", 
                "456789012345"
            );
        }

        @Test
        @DisplayName("Should ignore duplicate friend codes")
        void shouldIgnoreDuplicateFriendCodes() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Add me 123456789012", ""),
                createMockPost("My code: 123456789012", "Same code here: 123456789012")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).containsExactly("123456789012");
        }

        @Test
        @DisplayName("Should ignore invalid friend codes")
        void shouldIgnoreInvalidFriendCodes() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Invalid codes 12345 1234567890123 abc123456789", "Valid: 123456789012")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).containsExactly("123456789012");
        }

        @Test
        @DisplayName("Should return empty set when no friend codes found")
        void shouldReturnEmptySetWhenNoFriendCodesFound() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost("Just looking for friends", "No codes here")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).isEmpty();
        }

        @Test
        @DisplayName("Should handle null or empty posts gracefully")
        void shouldHandleNullOrEmptyPostsGracefully() {
            // Given
            RedditListingResponse mockResponse = createMockResponse(
                createMockPost(null, null),
                createMockPost("", ""),
                createMockPost("   ", "   ")
            );
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).isEmpty();
        }
    }

    @Nested
    @DisplayName("OAuth Authentication Tests")
    class OAuthAuthenticationTests {

        @Test
        @DisplayName("Should return empty set when OAuth token is unavailable")
        void shouldReturnEmptySetWhenOAuthTokenUnavailable() {
            // Given
            when(redditOAuthService.getAuthorizationHeader()).thenReturn(null);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).isEmpty();
        }

        @Test
        @DisplayName("Should return empty set when API returns non-2xx status")
        void shouldReturnEmptySetWhenApiReturnsErrorStatus() {
            // Given
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).isEmpty();
        }
    }

    @Nested
    @DisplayName("API Error Handling Tests")
    class ApiErrorHandlingTests {

        @Test
        @DisplayName("Should return empty set when API returns null response body")
        void shouldReturnEmptySetWhenApiReturnsNullResponseBody() {
            // Given
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(null, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).isEmpty();
        }

        @Test
        @DisplayName("Should return empty set when API throws exception")
        void shouldReturnEmptySetWhenApiThrowsException() {
            // Given
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenThrow(new RuntimeException("API error"));

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).isEmpty();
        }

        @Test
        @DisplayName("Should handle malformed Reddit response gracefully")
        void shouldHandleMalformedRedditResponseGracefully() {
            // Given
            RedditListingResponse malformedResponse = new RedditListingResponse();
            malformedResponse.setData(null);
            ResponseEntity<RedditListingResponse> responseEntity = 
                new ResponseEntity<>(malformedResponse, HttpStatus.OK);
            
            when(redditApiRestTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(RedditListingResponse.class)
            )).thenReturn(responseEntity);

            // When
            Set<String> friendCodes = redditService.fetchFriendCodes();

            // Then
            assertThat(friendCodes).isEmpty();
        }
    }

    // Helper methods for creating mock data
    private RedditListingResponse createMockResponse(RedditChild... children) {
        RedditListingResponse response = new RedditListingResponse();
        RedditListingData data = new RedditListingData();
        data.setChildren(Arrays.asList(children));
        response.setData(data);
        return response;
    }

    private RedditChild createMockPost(String title, String selftext) {
        RedditChild child = new RedditChild();
        RedditPost post = new RedditPost();
        post.setTitle(title);
        post.setSelftext(selftext);
        post.setAuthor("testuser");
        child.setData(post);
        return child;
    }
}
