package com.devs.simplicity.poke_go_friends.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Response DTO for paginated friend code feed.
 * Contains a list of friend codes and pagination information.
 */
@Data
@Builder
@Jacksonized
public class FriendCodeFeedResponse {

    private List<FriendCodeResponse> friendCodes;

    private boolean hasMore;

    private String nextCursor;

    private int totalElements;

    private int currentPage;

    private int pageSize;

    /**
     * Creates a feed response with pagination information.
     *
     * @param friendCodes   the list of friend codes for this page
     * @param hasMore       whether there are more pages available
     * @param nextCursor    cursor for the next page (can be null if no more pages)
     * @param totalElements total number of elements across all pages
     * @param currentPage   current page number (0-based)
     * @param pageSize      number of elements per page
     * @return a feed response with pagination information
     */
    public static FriendCodeFeedResponse of(List<FriendCodeResponse> friendCodes,
                                          boolean hasMore,
                                          String nextCursor,
                                          int totalElements,
                                          int currentPage,
                                          int pageSize) {
        return FriendCodeFeedResponse.builder()
                .friendCodes(friendCodes)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .totalElements(totalElements)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .build();
    }
}
