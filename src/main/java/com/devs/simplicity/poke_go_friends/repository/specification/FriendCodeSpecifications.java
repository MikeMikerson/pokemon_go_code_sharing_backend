package com.devs.simplicity.poke_go_friends.repository.specification;

import com.devs.simplicity.poke_go_friends.dto.FriendCodeSearchCriteria;
import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.entity.Team;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * JPA Specifications for building dynamic queries for FriendCode entity.
 * Provides type-safe and flexible query construction based on search criteria.
 */
public final class FriendCodeSpecifications {

    private FriendCodeSpecifications() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a Specification for active friend codes that haven't expired.
     *
     * @param currentTime Current timestamp to check expiration
     * @return Specification for active friend codes
     */
    public static Specification<FriendCode> isActive(LocalDateTime currentTime) {
        return (root, query, criteriaBuilder) -> {
            Predicate isActive = criteriaBuilder.isTrue(root.get("isActive"));
            Predicate notExpired = criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("expiresAt")),
                criteriaBuilder.greaterThan(root.get("expiresAt"), currentTime)
            );
            return criteriaBuilder.and(isActive, notExpired);
        };
    }

    /**
     * Creates a Specification for filtering by location.
     *
     * @param location Location to search for (case-insensitive partial match)
     * @return Specification for location filter
     */
    public static Specification<FriendCode> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(location)) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("location")),
                "%" + location.toLowerCase() + "%"
            );
        };
    }

    /**
     * Creates a Specification for filtering by team.
     *
     * @param team Team to filter by
     * @return Specification for team filter
     */
    public static Specification<FriendCode> hasTeam(Team team) {
        return (root, query, criteriaBuilder) -> {
            if (team == null) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.equal(root.get("team"), team);
        };
    }

    /**
     * Creates a Specification for filtering by minimum player level.
     *
     * @param minLevel Minimum player level (inclusive)
     * @return Specification for minimum level filter
     */
    public static Specification<FriendCode> hasMinimumLevel(Integer minLevel) {
        return (root, query, criteriaBuilder) -> {
            if (minLevel == null) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("playerLevel")),
                criteriaBuilder.greaterThanOrEqualTo(root.get("playerLevel"), minLevel)
            );
        };
    }

    /**
     * Creates a Specification for filtering by maximum player level.
     *
     * @param maxLevel Maximum player level (inclusive)
     * @return Specification for maximum level filter
     */
    public static Specification<FriendCode> hasMaximumLevel(Integer maxLevel) {
        return (root, query, criteriaBuilder) -> {
            if (maxLevel == null) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("playerLevel")),
                criteriaBuilder.lessThanOrEqualTo(root.get("playerLevel"), maxLevel)
            );
        };
    }

    /**
     * Creates a Specification for text search in trainer name and description.
     *
     * @param searchText Text to search for (case-insensitive partial match)
     * @return Specification for text search
     */
    public static Specification<FriendCode> containsText(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchText)) {
                return criteriaBuilder.conjunction(); // Always true
            }

            String searchPattern = "%" + searchText.toLowerCase() + "%";

            Predicate trainerNameMatch = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("trainerName")),
                searchPattern
            );

            Predicate descriptionMatch = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")),
                searchPattern
            );

            return criteriaBuilder.or(trainerNameMatch, descriptionMatch);
        };
    }

    /**
     * Creates a composite Specification based on search criteria.
     * Combines all applicable filters using AND logic.
     *
     * @param criteria Search criteria containing all filter parameters
     * @param currentTime Current timestamp for checking expiration
     * @return Combined Specification for all criteria
     */
    public static Specification<FriendCode> withCriteria(FriendCodeSearchCriteria criteria, LocalDateTime currentTime) {
        return isActive(currentTime)
                .and(hasLocation(criteria.getLocation()))
                .and(hasTeam(criteria.getTeam()))
                .and(hasMinimumLevel(criteria.getMinLevel()))
                .and(hasMaximumLevel(criteria.getMaxLevel()))
                .and(containsText(criteria.getSearchText()));
    }
}
