package com.devs.simplicity.poke_go_friends.controller;

import com.devs.simplicity.poke_go_friends.annotation.RateLimited;
import com.devs.simplicity.poke_go_friends.dto.CanSubmitResponse;
import com.devs.simplicity.poke_go_friends.dto.ErrorResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeFeedResponse;
import com.devs.simplicity.poke_go_friends.dto.FriendCodeSubmissionRequest;
import com.devs.simplicity.poke_go_friends.dto.SubmissionResponse;
import com.devs.simplicity.poke_go_friends.metrics.ApplicationMetricsService;
import com.devs.simplicity.poke_go_friends.service.FingerprintService;
import com.devs.simplicity.poke_go_friends.service.FriendCodeService;
import com.devs.simplicity.poke_go_friends.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * REST controller for managing Pokémon Go friend codes.
 * Provides endpoints for submitting, retrieving, and checking submission eligibility.
 */
@Slf4j
@RestController
@RequestMapping("/api/friend-codes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Friend Codes", description = "Pokémon Go Friend Code Management API")
public class FriendCodeController {

    private final FriendCodeService friendCodeService;
    private final RateLimitService rateLimitService;
    private final FingerprintService fingerprintService;
    private final ApplicationMetricsService metricsService;

    /**
     * Submits a new friend code after validation and rate limit checks.
     */
    @PostMapping
    @Operation(summary = "Submit a new friend code",
               description = "Submits a new Pokémon Go friend code with optional trainer information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Friend code submitted successfully",
                     content = @Content(schema = @Schema(implementation = SubmissionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> submitFriendCode(
            @Valid @RequestBody FriendCodeSubmissionRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Friend code submission request received");
        
        try {
            // Generate user fingerprint for the service
            String userFingerprint = fingerprintService.generateFingerprint(httpRequest);

            // Submit the friend code (rate limiting is handled by @RateLimited aspect)
            SubmissionResponse response = friendCodeService.submitFriendCode(request, userFingerprint);

            if (response.isSuccess()) {
                // Track successful submission
                metricsService.incrementSubmissions();
                log.info("Friend code submitted successfully");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.warn("Friend code submission failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(ErrorResponse.builder()
                        .error("Validation failed")
                        .message(response.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
            }
            
        } catch (Exception e) {
            log.error("Unexpected error during friend code submission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("Internal server error")
                            .message("An unexpected error occurred while processing your submission")
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    /**
     * Retrieves a paginated list of active friend codes.
     */
    @GetMapping
    @Operation(summary = "Get friend codes feed", 
               description = "Retrieves a paginated list of active friend codes, sorted by newest first")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Friend codes retrieved successfully",
                     content = @Content(schema = @Schema(implementation = FriendCodeFeedResponse.class))),
        @ApiResponse(responseCode = "304", description = "Not Modified"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getFriendCodes(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        log.debug("Friend codes feed request - page: {}, size: {}", page, size);
        try {
            FriendCodeFeedResponse response = friendCodeService.getActiveFriendCodes(page, size);
            String responseJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(response);
            String etag = "\"" + DigestUtils.md5DigestAsHex(responseJson.getBytes()) + "\"";
            if (etag.equals(ifNoneMatch)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
            }
            return ResponseEntity.ok().eTag(etag).body(response);
        } catch (Exception e) {
            log.error("Error retrieving friend codes feed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .error("Internal server error")
                            .message("An error occurred while retrieving friend codes")
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    /**
     * Checks if the user can submit a new friend code.
     */
    @GetMapping("/can-submit")
    @Operation(summary = "Check submission eligibility", 
               description = "Checks if the user can submit a new friend code based on rate limiting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Submission eligibility checked successfully",
                     content = @Content(schema = @Schema(implementation = CanSubmitResponse.class)))
    })
    public ResponseEntity<CanSubmitResponse> canSubmit(HttpServletRequest request) {
        log.debug("Checking submission eligibility");
        
        boolean canSubmit = rateLimitService.canSubmit(request);
        Instant nextSubmissionTime = rateLimitService.getNextAllowedSubmissionTime(request);
        
        CanSubmitResponse response = CanSubmitResponse.builder()
                .canSubmit(canSubmit)
                .nextSubmissionTime(nextSubmissionTime)
                .build();
        
        log.debug("User can submit: {}, next submission time: {}", canSubmit, nextSubmissionTime);
        
        return ResponseEntity.ok(response);
    }
}
