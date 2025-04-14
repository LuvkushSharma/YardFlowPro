package com.yardflowpro.controller;

import com.yardflowpro.dto.MoveRequestDto;
import com.yardflowpro.dto.MoveRequestResponseDto;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.MoveRequest.MoveStatus;
import com.yardflowpro.model.Site;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.service.MoveRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing trailer move requests.
 * <p>
 * Provides endpoints for creating, updating, and querying move requests
 * that represent the physical movement of trailers within a yard.
 * </p>
 */
@RestController
@RequestMapping("/api/move-requests")
@Slf4j
public class MoveRequestController {

    private final MoveRequestService moveRequestService;
    private final SiteRepository siteRepository;

    /**
     * Creates a new MoveRequestController with required dependencies.
     *
     * @param moveRequestService service for move request operations
     * @param siteRepository repository for site entities
     */
    @Autowired
    public MoveRequestController(
            MoveRequestService moveRequestService,
            SiteRepository siteRepository) {
        this.moveRequestService = moveRequestService;
        this.siteRepository = siteRepository;
    }

    /**
     * Creates a new move request.
     *
     * @param moveRequestDto move request details
     * @param requestedById ID of the user creating the request
     * @return the created move request
     */
    @PostMapping
    public ResponseEntity<MoveRequestResponseDto> createMoveRequest(
            @Valid @RequestBody MoveRequestDto moveRequestDto,
            @RequestParam Long requestedById) {
        log.info("API: Creating move request for trailer ID: {} by user ID: {}", 
                moveRequestDto.getTrailerId(), requestedById);
        
        MoveRequest moveRequest = moveRequestService.createMoveRequest(moveRequestDto, requestedById);
        return new ResponseEntity<>(moveRequestService.convertToDto(moveRequest), HttpStatus.CREATED);
    }

    /**
     * Retrieves all pending move requests.
     *
     * @return list of pending move requests
     */
    @GetMapping("/pending")
    public ResponseEntity<List<MoveRequestResponseDto>> getPendingMoveRequests() {
        log.info("API: Retrieving all pending move requests");
        List<MoveRequestResponseDto> pendingRequests = moveRequestService.getPendingMoveRequests().stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Retrieves a specific move request by ID.
     *
     * @param id ID of the move request
     * @return the move request
     */
    @GetMapping("/{id}")
    public ResponseEntity<MoveRequestResponseDto> getMoveRequestById(@PathVariable Long id) {
        log.info("API: Retrieving move request with ID: {}", id);
        MoveRequest moveRequest = moveRequestService.getMoveRequestById(id);
        return ResponseEntity.ok(moveRequestService.convertToDto(moveRequest));
    }

    /**
     * Retrieves all move requests assigned to a specific spotter.
     *
     * @param spotterId ID of the spotter
     * @return list of move requests assigned to the spotter
     */
    @GetMapping("/spotter/{spotterId}")
    public ResponseEntity<List<MoveRequestResponseDto>> getMoveRequestsBySpotterId(@PathVariable Long spotterId) {
        log.info("API: Retrieving move requests for spotter ID: {}", spotterId);
        List<MoveRequestResponseDto> spotterRequests = moveRequestService.getMoveRequestsBySpotterId(spotterId).stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(spotterRequests);
    }

    /**
     * Assigns a move request to a spotter.
     *
     * @param id ID of the move request
     * @param spotterId ID of the spotter
     * @return the updated move request
     */
    @PatchMapping("/{id}/assign/{spotterId}")
    public ResponseEntity<MoveRequestResponseDto> assignMoveRequest(
            @PathVariable Long id, 
            @PathVariable Long spotterId) {
        log.info("API: Assigning move request ID: {} to spotter ID: {}", id, spotterId);
        MoveRequest moveRequest = moveRequestService.assignMoveRequest(id, spotterId);
        return ResponseEntity.ok(moveRequestService.convertToDto(moveRequest));
    }

    /**
     * Updates a move request status to IN_PROGRESS.
     *
     * @param id ID of the move request
     * @return the updated move request
     */
    @PatchMapping("/{id}/start")
    public ResponseEntity<MoveRequestResponseDto> startMoveRequest(@PathVariable Long id) {
        log.info("API: Starting move request ID: {}", id);
        MoveRequest moveRequest = moveRequestService.startMoveRequest(id);
        return ResponseEntity.ok(moveRequestService.convertToDto(moveRequest));
    }

    /**
     * Marks a move request as completed and updates trailer state.
     *
     * @param id ID of the move request
     * @return the updated move request
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<MoveRequestResponseDto> completeMoveRequest(@PathVariable Long id) {
        log.info("API: Completing move request ID: {}", id);
        MoveRequest moveRequest = moveRequestService.completeMoveRequest(id);
        return ResponseEntity.ok(moveRequestService.convertToDto(moveRequest));
    }

    /**
     * Marks a move request as cancelled.
     *
     * @param id ID of the move request
     * @return the updated move request
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MoveRequestResponseDto> cancelMoveRequest(@PathVariable Long id) {
        log.info("API: Cancelling move request ID: {}", id);
        MoveRequest moveRequest = moveRequestService.cancelMoveRequest(id);
        return ResponseEntity.ok(moveRequestService.convertToDto(moveRequest));
    }

    /**
     * Adds notes to a move request.
     *
     * @param id ID of the move request
     * @param notes notes to add
     * @return the updated move request
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<MoveRequestResponseDto> addNotesToMoveRequest(
            @PathVariable Long id, 
            @RequestParam String notes) {
        log.info("API: Adding notes to move request ID: {}", id);
        MoveRequest moveRequest = moveRequestService.addNotesToMoveRequest(id, notes);
        return ResponseEntity.ok(moveRequestService.convertToDto(moveRequest));
    }

    /**
     * Retrieves all move requests at a specific site.
     *
     * @param siteId ID of the site
     * @return list of move requests at the site
     */
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<MoveRequestResponseDto>> getMoveRequestsBySite(@PathVariable Long siteId) {
        log.info("API: Retrieving move requests for site ID: {}", siteId);
        Site site = getSiteById(siteId);
        List<MoveRequestResponseDto> siteRequests = moveRequestService.getMoveRequestsBySite(site).stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(siteRequests);
    }

    /**
     * Retrieves all pending move requests at a specific site.
     *
     * @param siteId ID of the site
     * @return list of pending move requests at the site
     */
    @GetMapping("/site/{siteId}/pending")
    public ResponseEntity<List<MoveRequestResponseDto>> getPendingMoveRequestsBySite(@PathVariable Long siteId) {
        log.info("API: Retrieving pending move requests for site ID: {}", siteId);
        Site site = getSiteById(siteId);
        List<MoveRequestResponseDto> pendingSiteRequests = moveRequestService.getPendingMoveRequestsBySite(site).stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingSiteRequests);
    }

    /**
     * Retrieves all active move requests at a specific site.
     *
     * @param siteId ID of the site
     * @return list of active move requests at the site
     */
    @GetMapping("/site/{siteId}/active")
    public ResponseEntity<List<MoveRequestResponseDto>> getActiveMoveRequestsBySite(@PathVariable Long siteId) {
        log.info("API: Retrieving active move requests for site ID: {}", siteId);
        List<MoveRequestResponseDto> activeRequests = moveRequestService.getActiveMoveRequestsBySiteId(siteId).stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeRequests);
    }

    /**
     * Retrieves all move requests with a specific status.
     *
     * @param status the status to filter by
     * @return list of move requests with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MoveRequestResponseDto>> getMoveRequestsByStatus(
            @PathVariable MoveStatus status) {
        log.info("API: Retrieving move requests with status: {}", status);
        List<MoveRequestResponseDto> statusRequests = moveRequestService.getMoveRequestsByStatus(status).stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(statusRequests);
    }

    /**
     * Retrieves all move requests for a specific trailer.
     *
     * @param trailerId ID of the trailer
     * @return list of move requests for the trailer
     */
    @GetMapping("/trailer/{trailerId}")
    public ResponseEntity<List<MoveRequestResponseDto>> getMoveRequestsByTrailerId(@PathVariable Long trailerId) {
        log.info("API: Retrieving move requests for trailer ID: {}", trailerId);
        List<MoveRequestResponseDto> trailerRequests = moveRequestService.getMoveRequestsByTrailerId(trailerId).stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trailerRequests);
    }

    /**
     * Retrieves all active move requests (REQUESTED, ASSIGNED, or IN_PROGRESS).
     *
     * @return list of active move requests
     */
    @GetMapping("/active")
    public ResponseEntity<List<MoveRequestResponseDto>> getActiveMoveRequests() {
        log.info("API: Retrieving all active move requests");
        List<MoveRequestResponseDto> activeRequests = moveRequestService.getActiveMoveRequests().stream()
                .map(moveRequestService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeRequests);
    }

    /**
     * Retrieves move requests created within a date range.
     *
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of move requests created within the date range
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<List<MoveRequestResponseDto>> getMoveRequestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("API: Retrieving move requests between {} and {}", startDate, endDate);
        List<MoveRequestResponseDto> dateRangeRequests = 
                moveRequestService.getMoveRequestsByDateRange(startDate, endDate).stream()
                        .map(moveRequestService::convertToDto)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(dateRangeRequests);
    }

    /**
     * Helper method to retrieve a site by ID.
     */
    private Site getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
    }
}