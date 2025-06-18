package com.devs.simplicity.poke_go_friends.service;

import com.devs.simplicity.poke_go_friends.dto.FriendCodeFeedResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.dto.SubmissionResponse;
import com.devs.simplicity.poke_go_friends.mapper.FriendCodeMapper;
import com.devs.simplicity.poke_go_friends.model.FriendCode;
import com.devs.simplicity.poke_go_friends.repository.FriendCodeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Service class for managing friend code business logic.
 * Handles validation, submission, retrieval, and expiration management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendCodeService {

    private final FriendCodeRepository friendCodeRepository;
    private final FriendCodeMapper friendCodeMapper;
    private final Validator validator;

    /**
     * Submits a new friend code after validation.
     * Generates expiry timestamp (48 hours from submission).
     * 
     * @param request the friend code submission request
     * @param userFingerprint the user's fingerprint for rate limiting
     * @return submission response with success status and created friend code
     */
    @Transactional
    public SubmissionResponse submitFriendCode(FriendCodeSubmissionRequest request, String userFingerprint) {
        log.debug("Attempting to submit friend code for user fingerprint: {}", userFingerprint);
        
        try {
            // Convert request to entity
            FriendCode friendCode = friendCodeMapper.toEntity(request, userFingerprint);
            
            // Validate the entity
            Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);
            if (!violations.isEmpty()) {
                String errorMessage = violations.iterator().next().getMessage();
                log.warn("Validation failed for friend code submission: {}", errorMessage);
                return SubmissionResponse.validationError(errorMessage);
            }
            
            // Set expiry timestamp (48 hours from now)
            LocalDateTime now = LocalDateTime.now();
            friendCode.setSubmittedAt(now);
            friendCode.setExpiresAt(now.plusHours(48));
            
            // Save to database
            FriendCode savedFriendCode = friendCodeRepository.save(friendCode);
            log.info("Successfully submitted friend code with ID: {} for user: {}", 
                    savedFriendCode.getId(), userFingerprint);
            
            // Convert to response DTO
            FriendCodeResponse friendCodeResponse = friendCodeMapper.toResponse(savedFriendCode);
            
            // Calculate next submission allowed time (24 hours from now)
            LocalDateTime nextSubmissionAllowed = now.plusHours(24);
            
            return SubmissionResponse.success(friendCodeResponse, nextSubmissionAllowed);
            
        } catch (Exception e) {
            log.error("Error submitting friend code for user {}: {}", userFingerprint, e.getMessage(), e);
            return SubmissionResponse.validationError("Internal error occurred while processing submission");
        }
    }

    /**
     * Retrieves a paginated list of active (non-expired) friend codes.
     * Automatically filters out expired codes before returning.
     * 
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated response with active friend codes
     */
    @Cacheable(value = "friendCodesFeed", key = "#page + '-' + #size")
    @Transactional(readOnly = true)
    public FriendCodeFeedResponse getActiveFriendCodes(int page, int size) {
        log.debug("Retrieving active friend codes (projection) - page: {}, size: {}", page, size);
        LocalDateTime currentTime = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);
        Page<com.devs.simplicity.poke_go_friends.dto.projection.FriendCodeFeedProjection> projectionPage = friendCodeRepository.findActiveFriendCodesProjected(currentTime, pageable);
        List<FriendCodeResponse> friendCodeResponses = projectionPage.getContent().stream()
                .map(friendCodeMapper::fromFeedProjection)
                .toList();
        log.debug("Retrieved {} active friend codes for page {} (projection)", friendCodeResponses.size(), page);
        return FriendCodeFeedResponse.builder()
                .friendCodes(friendCodeResponses)
                .hasMore(projectionPage.hasNext())
                .totalElements((int) projectionPage.getTotalElements())
                .currentPage(page)
                .pageSize(size)
                .nextCursor(projectionPage.hasNext() ? String.valueOf(page + 1) : null)
                .build();
    }

    /**
     * Retrieves a specific friend code by ID if it exists and hasn't expired.
     * 
     * @param id the friend code ID
     * @return the friend code response, or null if not found or expired
     */
    @Transactional(readOnly = true)
    public FriendCodeResponse getFriendCodeById(String id) {
        log.debug("Retrieving friend code by ID: {}", id);
        
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            return friendCodeRepository.findById(uuid)
                    .filter(friendCode -> !friendCode.isExpired())
                    .map(friendCodeMapper::toResponse)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format provided: {}", id);
            return null;
        }
    }

    /**
     * Counts the total number of active (non-expired) friend codes.
     * 
     * @return the count of active friend codes
     */
    @Transactional(readOnly = true)
    public long countActiveFriendCodes() {
        LocalDateTime currentTime = LocalDateTime.now();
        long count = friendCodeRepository.countActiveFriendCodes(currentTime);
        log.debug("Active friend codes count: {}", count);
        return count;
    }

    /**
     * Validates all input fields according to business rules.
     * This method performs additional business validation beyond basic constraint validation.
     * 
     * @param request the friend code submission request to validate
     * @return true if valid, false otherwise
     */
    public boolean validateSubmissionRequest(FriendCodeSubmissionRequest request) {
        if (request == null) {
            return false;
        }
        
        // Convert to entity for validation
        FriendCode friendCode = friendCodeMapper.toEntity(request, "temp-fingerprint");
        Set<ConstraintViolation<FriendCode>> violations = validator.validate(friendCode);
        
        if (!violations.isEmpty()) {
            log.debug("Validation failed with {} violations", violations.size());
            return false;
        }
        
        return true;
    }

    /**
     * Checks if any friend codes have expired and need cleanup.
     * This is a read-only check that can be used by cleanup services.
     * 
     * @return true if there are expired codes that need cleanup
     */
    @Transactional(readOnly = true)
    public boolean hasExpiredCodes() {
        LocalDateTime currentTime = LocalDateTime.now();
        long totalCodes = friendCodeRepository.count();
        long activeCodes = friendCodeRepository.countActiveFriendCodes(currentTime);
        
        boolean hasExpired = totalCodes > activeCodes;
        log.debug("Total codes: {}, Active codes: {}, Has expired codes: {}", 
                totalCodes, activeCodes, hasExpired);
        
        return hasExpired;
    }
}
