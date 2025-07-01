package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.dto.*;
import com.devs.simplicity.poke_go_friends.entity.FriendCode;
import com.devs.simplicity.poke_go_friends.service.FriendCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for friend code operations.
 * Provides endpoints for creating, reading, updating, and deleting friend codes.
 */
@RestController
@RequestMapping("/api/friend-codes")
@RequiredArgsConstructor
@Slf4j
public class FriendCodeController {

    private final FriendCodeService friendCodeService;

    /**
     * Submit a new friend code.
     * POST /api/friend-codes
     */
    @PostMapping
    public ResponseEntity<FriendCodeResponse> submitFriendCode(
            @Valid @RequestBody FriendCodeSubmissionRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Received friend code submission for trainer: {}", request.getTrainerName());
        
        String ipAddress = getClientIpAddress(httpRequest);
        
        // For now, we'll support anonymous submissions (userId = null)
        // In the future, authentication can be added to extract userId from JWT token
        Long userId = null;
        
        FriendCode createdFriendCode = friendCodeService.createFriendCode(
            request.getFriendCode(),
            request.getTrainerName(),
            request.getPlayerLevel(),
            request.getLocation(),
            request.getDescription(),
            request.getTeam(),
            request.getGoals(),
            ipAddress,
            userId
        );
        
        FriendCodeResponse response = FriendCodeResponse.fromEntity(createdFriendCode);
        
        log.info("Successfully created friend code with ID: {}", createdFriendCode.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get paginated list of friend codes with optional filters.
     * GET /api/friend-codes
     */
    @GetMapping
    public ResponseEntity<FriendCodeFeedResponse> getFriendCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(required = false) Integer maxLevel,
            @RequestParam(required = false) String search) {
        
        log.debug("Fetching friend codes - page: {}, size: {}, location: {}, levels: {}-{}, search: {}", 
                 page, size, location, minLevel, maxLevel, search);
        
        // Validate and limit page size
        size = Math.min(size, 100); // Maximum 100 items per page
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FriendCode> friendCodesPage;
        
        // Use advanced search if any filters are provided
        if (StringUtils.hasText(location) || minLevel != null || maxLevel != null || StringUtils.hasText(search)) {
            friendCodesPage = friendCodeService.searchWithFilters(location, minLevel, maxLevel, search, pageable);
        } else {
            friendCodesPage = friendCodeService.getActiveFriendCodes(pageable);
        }
        
        FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(friendCodesPage);
        
        log.debug("Returning {} friend codes out of {} total", 
                 response.getContent().size(), response.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific friend code by ID.
     * GET /api/friend-codes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FriendCodeResponse> getFriendCode(@PathVariable Long id) {
        log.debug("Fetching friend code with ID: {}", id);
        
        FriendCode friendCode = friendCodeService.getFriendCodeById(id);
        FriendCodeResponse response = FriendCodeResponse.fromEntity(friendCode);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update a friend code (owner only).
     * PUT /api/friend-codes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<FriendCodeResponse> updateFriendCode(
            @PathVariable Long id,
            @Valid @RequestBody FriendCodeUpdateRequest request) {
        
        log.info("Updating friend code: {}", id);
        
        if (!request.hasAnyUpdate()) {
            log.warn("No update fields provided for friend code: {}", id);
            return ResponseEntity.badRequest().build();
        }
        
        // For now, we'll allow anonymous updates (userId = null)
        // In the future, authentication can be added to extract userId from JWT token
        Long userId = null;
        
        FriendCode updatedFriendCode = friendCodeService.updateFriendCode(
            id,
            request.getTrainerName(),
            request.getPlayerLevel(),
            request.getLocation(),
            request.getDescription(),
            request.getTeam(),
            request.getGoals(),
            userId
        );
        
        FriendCodeResponse response = FriendCodeResponse.fromEntity(updatedFriendCode);
        
        log.info("Successfully updated friend code: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a friend code (soft delete).
     * DELETE /api/friend-codes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateFriendCode(@PathVariable Long id) {
        log.info("Deactivating friend code: {}", id);
        
        // For now, we'll allow anonymous deactivation (userId = null)
        // In the future, authentication can be added to extract userId from JWT token
        Long userId = null;
        
        friendCodeService.deactivateFriendCode(id, userId);
        
        log.info("Successfully deactivated friend code: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search friend codes with query parameters.
     * GET /api/friend-codes/search
     */
    @GetMapping("/search")
    public ResponseEntity<FriendCodeFeedResponse> searchFriendCodes(
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(required = false) Integer maxLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Searching friend codes - trainer: {}, location: {}, levels: {}-{}", 
                 trainerName, location, minLevel, maxLevel);
        
        size = Math.min(size, 100); // Maximum 100 items per page
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FriendCode> friendCodesPage;
        
        // Use specific search methods based on provided parameters
        if (StringUtils.hasText(trainerName)) {
            friendCodesPage = friendCodeService.searchByTrainerName(trainerName, pageable);
        } else if (StringUtils.hasText(description)) {
            friendCodesPage = friendCodeService.searchByDescription(description, pageable);
        } else if (StringUtils.hasText(location)) {
            friendCodesPage = friendCodeService.getFriendCodesByLocation(location, pageable);
        } else if (minLevel != null || maxLevel != null) {
            friendCodesPage = friendCodeService.getFriendCodesByLevelRange(minLevel, maxLevel, pageable);
        } else {
            // If no specific search criteria, return active friend codes
            friendCodesPage = friendCodeService.getActiveFriendCodes(pageable);
        }
        
        FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(friendCodesPage);
        
        log.debug("Search returned {} friend codes", response.getContent().size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent friend code submissions.
     * GET /api/friend-codes/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<FriendCodeFeedResponse> getRecentFriendCodes(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching friend codes from last {} hours", hours);
        
        size = Math.min(size, 100);
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FriendCode> friendCodesPage = friendCodeService.getRecentSubmissions(hours, pageable);
        FriendCodeFeedResponse response = FriendCodeFeedResponse.fromPage(friendCodesPage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get friend code statistics.
     * GET /api/friend-codes/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getFriendCodeStats() {
        log.debug("Fetching friend code statistics");
        
        FriendCodeService.FriendCodeStats stats = friendCodeService.getStatistics();
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Extract client IP address from HTTP request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
