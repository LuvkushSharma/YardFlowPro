package com.yardflowpro.service.impl;

import com.yardflowpro.dto.MoveRequestDto;
import com.yardflowpro.dto.MoveRequestResponseDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.MoveRequest.MoveStatus;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.Trailer;
import com.yardflowpro.model.User;
import com.yardflowpro.repository.MoveRequestRepository;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.repository.TrailerRepository;
import com.yardflowpro.repository.UserRepository;
import com.yardflowpro.service.MoveRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the MoveRequestService interface for managing trailer move requests.
 * <p>
 * This implementation provides business logic for creating, assigning, and managing
 * the lifecycle of move requests, including validation and trailer state updates.
 * </p>
 */
@Service
@Slf4j
public class MoveRequestServiceImpl implements MoveRequestService {

    private final MoveRequestRepository moveRequestRepository;
    private final TrailerRepository trailerRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    /**
     * Creates a new MoveRequestServiceImpl with required repositories.
     *
     * @param moveRequestRepository repository for move request entities
     * @param trailerRepository repository for trailer entities
     * @param userRepository repository for user entities
     * @param siteRepository repository for site entities
     */
    @Autowired
    public MoveRequestServiceImpl(
            MoveRequestRepository moveRequestRepository,
            TrailerRepository trailerRepository,
            UserRepository userRepository,
            SiteRepository siteRepository) {
        this.moveRequestRepository = moveRequestRepository;
        this.trailerRepository = trailerRepository;
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MoveRequest createMoveRequest(MoveRequestDto moveRequestDto, Long requestedById) {
        log.info("Creating move request for trailer ID: {} requested by user ID: {}", 
                moveRequestDto.getTrailerId(), requestedById);
        
        validateMoveRequestDto(moveRequestDto);
        
        // Get entities
        Trailer trailer = getTrailerById(moveRequestDto.getTrailerId());
        User requestedBy = getUserById(requestedById);
        Site site = determineTrailerSite(trailer);
        
        // Validate site access
        validateUserSiteAccess(requestedBy, site);
        
        // Validate source and destination exist
        validateLocations(moveRequestDto, site);
        
        // Create move request
        MoveRequest moveRequest = new MoveRequest();
        moveRequest.setTrailer(trailer);
        moveRequest.setMoveType(moveRequestDto.getMoveType());
        moveRequest.setSourceLocationType(moveRequestDto.getSourceLocationType());
        moveRequest.setSourceLocationId(moveRequestDto.getSourceLocationId());
        moveRequest.setDestinationLocationType(moveRequestDto.getDestinationLocationType());
        moveRequest.setDestinationLocationId(moveRequestDto.getDestinationLocationId());
        moveRequest.setNotes(moveRequestDto.getNotes());
        moveRequest.setRequestedBy(requestedBy);
        moveRequest.setRequestTime(LocalDateTime.now());
        moveRequest.setStatus(MoveStatus.REQUESTED);
        moveRequest.setSite(site);
        
        MoveRequest savedRequest = moveRequestRepository.save(moveRequest);
        log.info("Created move request with ID: {} for trailer: {}", 
                savedRequest.getId(), trailer.getTrailerNumber());
        
        return savedRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MoveRequest assignMoveRequest(Long moveRequestId, Long spotterId) {
        log.info("Assigning move request ID: {} to spotter ID: {}", moveRequestId, spotterId);
        
        MoveRequest moveRequest = getMoveRequestById(moveRequestId);
        User spotter = getUserById(spotterId);
        
        // Validate status
        validateMoveRequestStatus(moveRequest, MoveStatus.REQUESTED, 
                "Cannot assign move request that is not in REQUESTED status");
        
        // Validate spotter role
        validateSpotterRole(spotter);
        
        // Validate site access
        validateUserSiteAccess(spotter, moveRequest.getSite());
        
        // Assign to spotter
        moveRequest.setAssignedSpotter(spotter);
        moveRequest.setAssignedTime(LocalDateTime.now());
        moveRequest.setStatus(MoveStatus.ASSIGNED);
        
        MoveRequest updatedRequest = moveRequestRepository.save(moveRequest);
        log.info("Successfully assigned move request ID: {} to spotter: {}", 
                moveRequestId, spotter.getUsername());
        
        return updatedRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MoveRequest startMoveRequest(Long moveRequestId) {
        log.info("Starting move request ID: {}", moveRequestId);
        
        MoveRequest moveRequest = getMoveRequestById(moveRequestId);
        
        // Validate status
        validateMoveRequestStatus(moveRequest, MoveStatus.ASSIGNED, 
                "Cannot start move request that is not in ASSIGNED status");
        
        // Update status
        moveRequest.setStartTime(LocalDateTime.now());
        moveRequest.setStatus(MoveStatus.IN_PROGRESS);
        
        MoveRequest updatedRequest = moveRequestRepository.save(moveRequest);
        log.info("Successfully started move request ID: {}", moveRequestId);
        
        return updatedRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MoveRequest completeMoveRequest(Long moveRequestId) {
        log.info("Completing move request ID: {}", moveRequestId);
        
        MoveRequest moveRequest = getMoveRequestById(moveRequestId);
        
        // Validate status
        validateMoveRequestStatus(moveRequest, MoveStatus.IN_PROGRESS, 
                "Cannot complete move request that is not in IN_PROGRESS status");
        
        // Update status
        moveRequest.setCompletionTime(LocalDateTime.now());
        moveRequest.setStatus(MoveStatus.COMPLETED);
        
        // Apply automation rules
        updateTrailerAfterMove(moveRequest);
        
        MoveRequest updatedRequest = moveRequestRepository.save(moveRequest);
        log.info("Successfully completed move request ID: {}", moveRequestId);
        
        return updatedRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MoveRequest cancelMoveRequest(Long moveRequestId) {
        log.info("Cancelling move request ID: {}", moveRequestId);
        
        MoveRequest moveRequest = getMoveRequestById(moveRequestId);
        
        // Validate status
        if (moveRequest.getStatus() == MoveStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel a completed move request");
        }
        
        // Update status
        moveRequest.setStatus(MoveStatus.CANCELLED);
        
        MoveRequest updatedRequest = moveRequestRepository.save(moveRequest);
        log.info("Successfully cancelled move request ID: {}", moveRequestId);
        
        return updatedRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MoveRequest addNotesToMoveRequest(Long moveRequestId, String notes) {
        log.info("Adding notes to move request ID: {}", moveRequestId);
        
        if (notes == null || notes.trim().isEmpty()) {
            throw new InvalidOperationException("Notes cannot be empty");
        }
        
        MoveRequest moveRequest = getMoveRequestById(moveRequestId);
        
        // Append or set notes
        if (moveRequest.getNotes() != null && !moveRequest.getNotes().trim().isEmpty()) {
            moveRequest.setNotes(moveRequest.getNotes() + "\n" + notes);
        } else {
            moveRequest.setNotes(notes);
        }
        
        MoveRequest updatedRequest = moveRequestRepository.save(moveRequest);
        log.info("Successfully added notes to move request ID: {}", moveRequestId);
        
        return updatedRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getPendingMoveRequests() {
        log.debug("Retrieving all pending move requests");
        return moveRequestRepository.findByStatus(MoveStatus.REQUESTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getMoveRequestsBySpotterId(Long spotterId) {
        log.debug("Retrieving move requests for spotter ID: {}", spotterId);
        User spotter = getUserById(spotterId);
        return moveRequestRepository.findByAssignedSpotter(spotter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MoveRequest getMoveRequestById(Long id) {
        log.debug("Retrieving move request with ID: {}", id);
        return moveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Move request not found with id: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getMoveRequestsBySite(Site site) {
        log.debug("Retrieving move requests for site: {}", site.getName());
        return moveRequestRepository.findBySite(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getPendingMoveRequestsBySite(Site site) {
        log.debug("Retrieving pending move requests for site: {}", site.getName());
        return moveRequestRepository.findBySiteAndStatus(site, MoveStatus.REQUESTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getMoveRequestsByTrailerId(Long trailerId) {
        log.debug("Retrieving move requests for trailer ID: {}", trailerId);
        Trailer trailer = getTrailerById(trailerId);
        return moveRequestRepository.findByTrailer(trailer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getMoveRequestsByStatus(MoveStatus status) {
        log.debug("Retrieving move requests with status: {}", status);
        return moveRequestRepository.findByStatus(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getActiveMoveRequests() {
        log.debug("Retrieving all active move requests");
        return moveRequestRepository.findActiveMoveRequests();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getActiveMoveRequestsBySiteId(Long siteId) {
        log.debug("Retrieving active move requests for site ID: {}", siteId);
        Site site = getSiteById(siteId);
        return moveRequestRepository.findActiveMoveRequestsBySite(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<MoveRequest> getMoveRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Retrieving move requests between {} and {}", startDate, endDate);
        
        if (startDate == null || endDate == null) {
            throw new InvalidOperationException("Both start and end dates are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new InvalidOperationException("Start date cannot be after end date");
        }
        
        return moveRequestRepository.findByRequestTimeBetween(startDate, endDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MoveRequestResponseDto convertToDto(MoveRequest moveRequest) {
        MoveRequestResponseDto dto = new MoveRequestResponseDto();
        
        dto.setId(moveRequest.getId());
        dto.setMoveType(moveRequest.getMoveType().name());
        dto.setStatus(moveRequest.getStatus().name());
        dto.setSourceLocationType(moveRequest.getSourceLocationType());
        dto.setSourceLocationId(moveRequest.getSourceLocationId());
        dto.setDestinationLocationType(moveRequest.getDestinationLocationType());
        dto.setDestinationLocationId(moveRequest.getDestinationLocationId());
        dto.setNotes(moveRequest.getNotes());
        
        // Set timestamps
        dto.setRequestTime(moveRequest.getRequestTime());
        dto.setAssignedTime(moveRequest.getAssignedTime());
        dto.setStartTime(moveRequest.getStartTime());
        dto.setCompletionTime(moveRequest.getCompletionTime());
        
        // Set related entities
        if (moveRequest.getSite() != null) {
            dto.setSiteId(moveRequest.getSite().getId());
            dto.setSiteName(moveRequest.getSite().getName());
        }
        
        if (moveRequest.getTrailer() != null) {
            dto.setTrailerId(moveRequest.getTrailer().getId());
            dto.setTrailerNumber(moveRequest.getTrailer().getTrailerNumber());
        }
        
        if (moveRequest.getRequestedBy() != null) {
            dto.setRequestedById(moveRequest.getRequestedBy().getId());
            dto.setRequestedByName(moveRequest.getRequestedBy().getUsername());
        }
        
        if (moveRequest.getAssignedSpotter() != null) {
            dto.setAssignedSpotterId(moveRequest.getAssignedSpotter().getId());
            dto.setAssignedSpotterName(moveRequest.getAssignedSpotter().getUsername());
        }
        
        return dto;
    }

    // -------------------------------------------------------------------------
    // Private helper methods for validation
    // -------------------------------------------------------------------------

    /**
     * Validates that a move request DTO contains all required fields.
     */
    private void validateMoveRequestDto(MoveRequestDto dto) {
        if (dto.getTrailerId() == null) {
            throw new InvalidOperationException("Trailer ID is required");
        }
        
        if (dto.getMoveType() == null) {
            throw new InvalidOperationException("Move type is required");
        }
        
        if (dto.getSourceLocationType() == null || dto.getSourceLocationType().trim().isEmpty()) {
            throw new InvalidOperationException("Source location type is required");
        }
        
        if (dto.getDestinationLocationType() == null || dto.getDestinationLocationType().trim().isEmpty()) {
            throw new InvalidOperationException("Destination location type is required");
        }
        
        if (!isValidLocationType(dto.getSourceLocationType())) {
            throw new InvalidOperationException("Invalid source location type: " + dto.getSourceLocationType());
        }
        
        if (!isValidLocationType(dto.getDestinationLocationType())) {
            throw new InvalidOperationException("Invalid destination location type: " + dto.getDestinationLocationType());
        }
        
        if (dto.getSourceLocationId() == null) {
            throw new InvalidOperationException("Source location ID is required");
        }
        
        if (dto.getDestinationLocationId() == null) {
            throw new InvalidOperationException("Destination location ID is required");
        }
    }
    
    /**
     * Checks if a location type is valid (YARD, DOOR, or GATE).
     */
    private boolean isValidLocationType(String locationType) {
        return "YARD".equals(locationType) || "DOOR".equals(locationType) || "GATE".equals(locationType);
    }
    
    /**
     * Determines the site where a trailer is currently located.
     */
    private Site determineTrailerSite(Trailer trailer) {
        if (trailer.getCurrentAppointment() != null && trailer.getCurrentAppointment().getSite() != null) {
            return trailer.getCurrentAppointment().getSite();
        }
        
        throw new InvalidOperationException(
                "Unable to determine site for trailer: " + trailer.getTrailerNumber() + 
                ". Trailer must have an active appointment.");
    }
    
    /**
     * Validates that a user has access to a specific site.
     */
    private void validateUserSiteAccess(User user, Site site) {
        if (site == null) {
            throw new InvalidOperationException("Site must be specified");
        }
        
        if (!userHasAccessToSite(user, site.getId())) {
            throw new InvalidOperationException(
                    "User " + user.getUsername() + " does not have access to site: " + site.getName());
        }
    }
    
    /**
     * Checks if a user has access to a specific site.
     */
    private boolean userHasAccessToSite(User user, Long siteId) {
        if (user.getRole() == User.UserRole.SUPERUSER || user.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        return user.getAccessibleSites().stream()
                .anyMatch(site -> site.getId().equals(siteId));
    }
    
    /**
     * Validates that a user has the SPOTTER role.
     */
    private void validateSpotterRole(User user) {
        if (user.getRole() != User.UserRole.SPOTTER) {
            throw new InvalidOperationException(
                    "User " + user.getUsername() + " is not a spotter. Role: " + user.getRole());
        }
    }
    
    /**
     * Validates that a move request has the expected status.
     */
    private void validateMoveRequestStatus(MoveRequest moveRequest, MoveStatus expectedStatus, String errorMessage) {
        if (moveRequest.getStatus() != expectedStatus) {
            throw new InvalidOperationException(
                    errorMessage + ". Current status: " + moveRequest.getStatus());
        }
    }
    
    /**
     * Validates that source and destination locations exist.
     */
    private void validateLocations(MoveRequestDto dto, Site site) {
        // Add validation logic for locations based on site
        // This is a placeholder for more complex validation
        log.debug("Validating locations for move request at site: {}", site.getName());
    }
    
    /**
     * Updates trailer state after a move is completed.
     */
    private void updateTrailerAfterMove(MoveRequest moveRequest) {
        Trailer trailer = moveRequest.getTrailer();
        
        if (moveRequest.getMoveType() == MoveRequest.MoveType.SPOT && 
                "DOOR".equals(moveRequest.getDestinationLocationType())) {
            
            // Update trailer when spotted to a door
            updateTrailerSpottedToDoor(trailer);
            
        } else if (moveRequest.getMoveType() == MoveRequest.MoveType.PULL && 
                "DOOR".equals(moveRequest.getSourceLocationType())) {
            
            // Update trailer when pulled from a door
            updateTrailerPulledFromDoor(trailer);
        }
    }
    
    /**
     * Updates trailer status when spotted to a door.
     */
    private void updateTrailerSpottedToDoor(Trailer trailer) {
        if (trailer.getLoadStatus() == Trailer.LoadStatus.FULL) {
            log.info("Updating FULL trailer process status to UNLOADING");
            trailer.setProcessStatus(Trailer.ProcessStatus.UNLOADING);
        } else if (trailer.getLoadStatus() == Trailer.LoadStatus.EMPTY) {
            log.info("Updating EMPTY trailer process status to LOADING");
            trailer.setProcessStatus(Trailer.ProcessStatus.LOADING);
        } else if (trailer.getLoadStatus() == Trailer.LoadStatus.PARTIAL) {
            log.info("Updating PARTIAL trailer process status to LOADING");
            trailer.setProcessStatus(Trailer.ProcessStatus.LOADING);
        }
        
        trailerRepository.save(trailer);
    }
    
    /**
     * Updates trailer status when pulled from a door.
     */
    private void updateTrailerPulledFromDoor(Trailer trailer) {
        if (trailer.getProcessStatus() == Trailer.ProcessStatus.LOADING) {
            log.info("Updating trailer from LOADING to LOADED and setting load status to FULL");
            trailer.setProcessStatus(Trailer.ProcessStatus.LOADED);
            trailer.setLoadStatus(Trailer.LoadStatus.FULL);
        } else if (trailer.getProcessStatus() == Trailer.ProcessStatus.UNLOADING) {
            log.info("Updating trailer from UNLOADING to UNLOADED and setting load status to EMPTY");
            trailer.setProcessStatus(Trailer.ProcessStatus.UNLOADED);
            trailer.setLoadStatus(Trailer.LoadStatus.EMPTY);
        }
        
        trailerRepository.save(trailer);
    }
    
    // -------------------------------------------------------------------------
    // Private helper methods for entity retrieval
    // -------------------------------------------------------------------------
    
    /**
     * Retrieves a trailer by ID.
     */
    private Trailer getTrailerById(Long trailerId) {
        return trailerRepository.findById(trailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + trailerId));
    }
    
    /**
     * Retrieves a user by ID.
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
    
    /**
     * Retrieves a site by ID.
     */
    private Site getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
    }
}