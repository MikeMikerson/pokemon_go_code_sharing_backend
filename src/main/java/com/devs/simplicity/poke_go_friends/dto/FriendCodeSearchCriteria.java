package com.devs.simplicity.poke_go_friends.dto;

import com.devs.simplicity.poke_go_friends.entity.Team;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for friend code search criteria.
 * Encapsulates all possible search and filter parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendCodeSearchCriteria {

    /**
     * Location filter - searches in the location field
     */
    private String location;

    /**
     * Team filter - filters by Pokemon Go team
     */
    private Team team;

    /**
     * Minimum player level filter
     */
    private Integer minLevel;

    /**
     * Maximum player level filter
     */
    private Integer maxLevel;

    /**
     * General search text - searches in trainer name and description
     */
    private String searchText;

    /**
     * Check if any filter criteria is specified
     * @return true if at least one filter is specified, false otherwise
     */
    public boolean hasFilters() {
        return location != null ||
               team != null ||
               minLevel != null ||
               maxLevel != null ||
               searchText != null;
    }
}
