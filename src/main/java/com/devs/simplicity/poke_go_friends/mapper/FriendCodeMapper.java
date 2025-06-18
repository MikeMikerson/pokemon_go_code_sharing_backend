package com.devs.simplicity.poke_go_friends.mapper;

import com.devs.simplicity.poke_go_friends.dto.FriendCodeResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.model.FriendCode;
import com.devs.simplicity.poke_go_friends.dto.projection.FriendCodeFeedProjection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapper for converting between FriendCode entities and DTOs.
 * Provides mapping functionality without external dependencies.
 */
@Component
public class FriendCodeMapper {

    /**
     * Converts a FriendCodeSubmissionRequest to a FriendCode entity.
     *
     * @param request         the submission request
     * @param userFingerprint the user's fingerprint for rate limiting
     * @return a new FriendCode entity
     */
    public FriendCode toEntity(FriendCodeSubmissionRequest request, String userFingerprint) {
        return FriendCode.builder()
                .friendCode(request.getFriendCode())
                .trainerName(request.getTrainerName())
                .trainerLevel(request.getTrainerLevel())
                .team(request.getTeam())
                .country(request.getCountry())
                .purpose(request.getPurpose())
                .message(request.getMessage())
                .userFingerprint(userFingerprint)
                .build();
    }

    /**
     * Converts a FriendCode entity to a FriendCodeResponse DTO.
     *
     * @param entity the FriendCode entity
     * @return a FriendCodeResponse DTO
     */
    public FriendCodeResponse toResponse(FriendCode entity) {
        if (entity == null) {
            return null;
        }

        return FriendCodeResponse.builder()
                .id(entity.getId())
                .friendCode(entity.getFriendCode())
                .trainerName(entity.getTrainerName())
                .trainerLevel(entity.getTrainerLevel())
                .team(entity.getTeam())
                .country(entity.getCountry())
                .purpose(entity.getPurpose())
                .message(entity.getMessage())
                .submittedAt(entity.getSubmittedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    /**
     * Converts a list of FriendCode entities to a list of FriendCodeResponse DTOs.
     *
     * @param entities the list of FriendCode entities
     * @return a list of FriendCodeResponse DTOs
     */
    public List<FriendCodeResponse> toResponseList(List<FriendCode> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Updates an existing FriendCode entity with data from a submission request.
     * This is useful for partial updates while preserving system-generated fields.
     *
     * @param entity  the existing FriendCode entity
     * @param request the submission request with new data
     * @param userFingerprint the user's fingerprint
     * @return the updated entity
     */
    public FriendCode updateEntity(FriendCode entity, FriendCodeSubmissionRequest request, String userFingerprint) {
        entity.setFriendCode(request.getFriendCode());
        entity.setTrainerName(request.getTrainerName());
        entity.setTrainerLevel(request.getTrainerLevel());
        entity.setTeam(request.getTeam());
        entity.setCountry(request.getCountry());
        entity.setPurpose(request.getPurpose());
        entity.setMessage(request.getMessage());
        entity.setUserFingerprint(userFingerprint);
        LocalDateTime now = LocalDateTime.now();
        entity.setSubmittedAt(now);
        entity.setExpiresAt(now.plusHours(48));
        
        return entity;
    }

    /**
     * Converts a FriendCodeFeedProjection to FriendCodeResponse.
     */
    public FriendCodeResponse fromFeedProjection(FriendCodeFeedProjection projection) {
        return FriendCodeResponse.builder()
                .id(projection.getId())
                .friendCode(projection.getFriendCode())
                .trainerName(projection.getTrainerName())
                .trainerLevel(projection.getTrainerLevel())
                .team(projection.getTeam())
                .country(projection.getCountry())
                .purpose(projection.getPurpose())
                .message(projection.getMessage())
                .submittedAt(projection.getSubmittedAt())
                .expiresAt(projection.getExpiresAt())
                .build();
    }
}
