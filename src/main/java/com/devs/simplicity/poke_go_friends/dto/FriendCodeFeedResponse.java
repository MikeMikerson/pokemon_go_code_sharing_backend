package com.devs.simplicity.poke_go_friends.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * DTO for paginated friend code feed responses.
 * Includes pagination metadata along with the data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendCodeFeedResponse {

    private List<FriendCodeResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    /**
     * Factory method to create feed response from Spring Data Page.
     */
    public static FriendCodeFeedResponse fromPage(org.springframework.data.domain.Page<com.devs.simplicity.poke_go_friends.entity.FriendCode> page) {
        List<FriendCodeResponse> content = page.getContent().stream()
                .map(FriendCodeResponse::fromEntity)
                .toList();

        return new FriendCodeFeedResponse(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }

    /**
     * Constructor for simple responses.
     */
    public FriendCodeFeedResponse(List<FriendCodeResponse> content) {
        this.content = content;
        this.totalElements = content.size();
        this.empty = content.isEmpty();
    }
}
