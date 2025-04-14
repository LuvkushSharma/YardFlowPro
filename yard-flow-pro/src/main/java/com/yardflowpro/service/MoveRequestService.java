package com.yardflowpro.service;

import com.yardflowpro.dto.MoveRequestDto;
import com.yardflowpro.dto.MoveRequestResponseDto;
import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.MoveRequest.MoveStatus;
import com.yardflowpro.model.Site;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing trailer move requests within yard management.
 * <p>
 * Provides methods for creating, updating, and querying move requests that represent
 * the movement of trailers between yard locations, doors, and gates.
 * </p>
 */
public interface MoveRequestService {
    
    /**
     * Creates a new move request for a trailer.
     *
     * @param moveRequestDto data transfer object containing move request details
     * @param requestedById ID of the user creating the request
     * @return the created move request
     * @throws com.yardflowpro.exception.ResourceNotFoundException if trailer or user not found
     * @throws com.yardflowpro.exception.InvalidOperationException if user doesn't have site access
     */
    MoveRequest createMoveRequest(MoveRequestDto moveRequestDto, Long requestedById);
    
    /**
     * Assigns a move request to a spotter for execution.
     *
     * @param moveRequestId ID of the move request to assign
     * @param spotterId ID of the spotter to assign the request to
     * @return the updated move request
     * @throws com.yardflowpro.exception.ResourceNotFoundException if move request or spotter not found
     * @throws com.yardflowpro.exception.InvalidOperationException if move request is not in REQUESTED status
     *                                                            or if user is not a spotter
     */
    MoveRequest assignMoveRequest(Long moveRequestId, Long spotterId);
    
    /**
     * Updates a move request status to IN_PROGRESS.
     *
     * @param moveRequestId ID of the move request to start
     * @return the updated move request
     * @throws com.yardflowpro.exception.ResourceNotFoundException if move request not found
     * @throws com.yardflowpro.exception.InvalidOperationException if move request is not in ASSIGNED status
     */
    MoveRequest startMoveRequest(Long moveRequestId);
    
    /**
     * Marks a move request as completed and updates trailer state accordingly.
     *
     * @param moveRequestId ID of the move request to complete
     * @return the updated move request
     * @throws com.yardflowpro.exception.ResourceNotFoundException if move request not found
     * @throws com.yardflowpro.exception.InvalidOperationException if move request is not in IN_PROGRESS status
     */
    MoveRequest completeMoveRequest(Long moveRequestId);
    
    /**
     * Marks a move request as cancelled.
     *
     * @param moveRequestId ID of the move request to cancel
     * @return the updated move request
     * @throws com.yardflowpro.exception.ResourceNotFoundException if move request not found
     * @throws com.yardflowpro.exception.InvalidOperationException if move request is already completed
     */
    MoveRequest cancelMoveRequest(Long moveRequestId);
    
    /**
     * Adds notes to an existing move request.
     *
     * @param moveRequestId ID of the move request to update
     * @param notes notes to add to the move request
     * @return the updated move request
     * @throws com.yardflowpro.exception.ResourceNotFoundException if move request not found
     */
    MoveRequest addNotesToMoveRequest(Long moveRequestId, String notes);
    
    /**
     * Retrieves all move requests with status REQUESTED.
     *
     * @return list of pending move requests
     */
    List<MoveRequest> getPendingMoveRequests();
    
    /**
     * Retrieves all move requests assigned to a specific spotter.
     *
     * @param spotterId ID of the spotter
     * @return list of move requests assigned to the spotter
     * @throws com.yardflowpro.exception.ResourceNotFoundException if spotter not found
     */
    List<MoveRequest> getMoveRequestsBySpotterId(Long spotterId);
    
    /**
     * Retrieves a specific move request by ID.
     *
     * @param id ID of the move request to retrieve
     * @return the move request
     * @throws com.yardflowpro.exception.ResourceNotFoundException if move request not found
     */
    MoveRequest getMoveRequestById(Long id);
    
    /**
     * Retrieves all move requests at a specific site.
     *
     * @param site the site to query for move requests
     * @return list of move requests at the site
     */
    List<MoveRequest> getMoveRequestsBySite(Site site);
    
    /**
     * Retrieves all move requests with status REQUESTED at a specific site.
     *
     * @param site the site to query for pending move requests
     * @return list of pending move requests at the site
     */
    List<MoveRequest> getPendingMoveRequestsBySite(Site site);
    
    /**
     * Retrieves move requests for a specific trailer.
     *
     * @param trailerId ID of the trailer
     * @return list of move requests for the trailer
     * @throws com.yardflowpro.exception.ResourceNotFoundException if trailer not found
     */
    List<MoveRequest> getMoveRequestsByTrailerId(Long trailerId);
    
    /**
     * Retrieves move requests with a specific status.
     *
     * @param status the status to filter by
     * @return list of move requests with the specified status
     */
    List<MoveRequest> getMoveRequestsByStatus(MoveStatus status);
    
    /**
     * Retrieves active move requests (REQUESTED, ASSIGNED, or IN_PROGRESS).
     *
     * @return list of active move requests
     */
    List<MoveRequest> getActiveMoveRequests();
    
    /**
     * Retrieves active move requests at a specific site.
     *
     * @param siteId ID of the site
     * @return list of active move requests at the site
     * @throws com.yardflowpro.exception.ResourceNotFoundException if site not found
     */
    List<MoveRequest> getActiveMoveRequestsBySiteId(Long siteId);
    
    /**
     * Retrieves move requests created within a date range.
     *
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of move requests created within the date range
     * @throws com.yardflowpro.exception.InvalidOperationException if date range is invalid
     */
    List<MoveRequest> getMoveRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Converts a MoveRequest entity to a MoveRequestResponseDto.
     *
     * @param moveRequest the move request entity to convert
     * @return the move request DTO
     */
    MoveRequestResponseDto convertToDto(MoveRequest moveRequest);
}