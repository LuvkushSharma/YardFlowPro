package com.yardflowpro.service.impl;

import com.yardflowpro.dto.MoveRequestDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.Trailer;
import com.yardflowpro.model.User;
import com.yardflowpro.repository.MoveRequestRepository;
import com.yardflowpro.repository.TrailerRepository;
import com.yardflowpro.repository.UserRepository;
import com.yardflowpro.service.MoveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class MoveRequestServiceImpl implements MoveRequestService {

    private final MoveRequestRepository moveRequestRepository;
    private final TrailerRepository trailerRepository;
    private final UserRepository userRepository;

    @Autowired
    public MoveRequestServiceImpl(
            MoveRequestRepository moveRequestRepository,
            TrailerRepository trailerRepository,
            UserRepository userRepository) {
        this.moveRequestRepository = moveRequestRepository;
        this.trailerRepository = trailerRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public MoveRequest createMoveRequest(MoveRequestDto moveRequestDto, Long requestedById) {
        // Validate trailer exists
        Trailer trailer = trailerRepository.findById(moveRequestDto.getTrailerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + moveRequestDto.getTrailerId()));
        
        // Validate requester exists
        User requestedBy = userRepository.findById(requestedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestedById));

        // Get the site for this move request
        Site site = null;

        // Determine the site based on trailer's current appointment
        if (trailer.getCurrentAppointment() != null && trailer.getCurrentAppointment().getSite() != null) {
            site = trailer.getCurrentAppointment().getSite();
        }
        
        // Validate that the requester has access to this site
        if (site != null && !userHasAccessToSite(requestedBy, site.getId())) {
            throw new InvalidOperationException("User does not have access to the site where this trailer is located");
        }
        
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
        moveRequest.setStatus(MoveRequest.MoveStatus.REQUESTED);
        moveRequest.setSite(site);
        
        return moveRequestRepository.save(moveRequest);
    }

    @Override
    @Transactional
    public MoveRequest assignMoveRequest(Long moveRequestId, Long spotterId) {
        MoveRequest moveRequest = moveRequestRepository.findById(moveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Move request not found with id: " + moveRequestId));
        
        // Validate status
        if (moveRequest.getStatus() != MoveRequest.MoveStatus.REQUESTED) {
            throw new InvalidOperationException("Move request is not in REQUESTED status");
        }
        
        // Validate spotter exists and has the SPOTTER role
        User spotter = userRepository.findById(spotterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + spotterId));
        
        if (spotter.getRole() != User.UserRole.SPOTTER) {
            throw new InvalidOperationException("User is not a spotter");
        }

        // Get the site for this move request
        Site site = null;

        // Determine the site based on trailer's current appointment
        if (moveRequest.getTrailer() != null && 
            moveRequest.getTrailer().getCurrentAppointment() != null && 
            moveRequest.getTrailer().getCurrentAppointment().getSite() != null) {
            site = moveRequest.getTrailer().getCurrentAppointment().getSite();
        }

        // Validate that the spotter has access to this site
        if (site != null && !userHasAccessToSite(spotter, site.getId())) {
            throw new InvalidOperationException("Spotter does not have access to the site where this trailer is located");
        }
        
        // Assign to spotter
        moveRequest.setAssignedSpotter(spotter);
        moveRequest.setAssignedTime(LocalDateTime.now());
        moveRequest.setStatus(MoveRequest.MoveStatus.ASSIGNED);
        
        return moveRequestRepository.save(moveRequest);
    }

    // Helper method to check if a user has access to a site
    private boolean userHasAccessToSite(User user, Long siteId) {
        if (user.getRole() == User.UserRole.SUPERUSER || user.getRole() == User.UserRole.ADMIN) {
            // Superusers and admins have access to all sites
            return true;
        }
        
        // For other users, check their explicitly assigned sites
        return user.getAccessibleSites().stream()
                .anyMatch(site -> site.getId().equals(siteId));
    }

    @Override
    @Transactional
    public MoveRequest startMoveRequest(Long moveRequestId) {
        MoveRequest moveRequest = moveRequestRepository.findById(moveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Move request not found with id: " + moveRequestId));
        
        // Validate status
        if (moveRequest.getStatus() != MoveRequest.MoveStatus.ASSIGNED) {
            throw new InvalidOperationException("Move request is not in ASSIGNED status");
        }
        
        // Update status
        moveRequest.setStartTime(LocalDateTime.now());
        moveRequest.setStatus(MoveRequest.MoveStatus.IN_PROGRESS);
        
        return moveRequestRepository.save(moveRequest);
    }

    @Override
    @Transactional
    public MoveRequest completeMoveRequest(Long moveRequestId) {
        MoveRequest moveRequest = moveRequestRepository.findById(moveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Move request not found with id: " + moveRequestId));
        
        // Validate status
        if (moveRequest.getStatus() != MoveRequest.MoveStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Move request is not in IN_PROGRESS status");
        }
        
        // Update status
        moveRequest.setCompletionTime(LocalDateTime.now());
        moveRequest.setStatus(MoveRequest.MoveStatus.COMPLETED);
        
        // Get the trailer
        Trailer trailer = moveRequest.getTrailer();
        
        // Apply automation rules based on move type and locations
        if (moveRequest.getMoveType() == MoveRequest.MoveType.SPOT && 
                "DOOR".equals(moveRequest.getDestinationLocationType())) {
            
            // When trailer is spotted to a door, update process status based on load status
            if (trailer.getLoadStatus() == Trailer.LoadStatus.FULL) {
                trailer.setProcessStatus(Trailer.ProcessStatus.UNLOADING);
            } else if (trailer.getLoadStatus() == Trailer.LoadStatus.EMPTY) {
                trailer.setProcessStatus(Trailer.ProcessStatus.LOADING);
            } else if (trailer.getLoadStatus() == Trailer.LoadStatus.PARTIAL) {
                // For partial loads, decide based on context or set a default
                // You might want to implement additional logic here
                trailer.setProcessStatus(Trailer.ProcessStatus.LOADING);
            }
            
            trailerRepository.save(trailer);
        }
        // Existing logic for PULL requests
        else if (moveRequest.getMoveType() == MoveRequest.MoveType.PULL && 
                "DOOR".equals(moveRequest.getSourceLocationType())) {
            
            // Apply automation rules when pulled from door
            if (trailer.getProcessStatus() == Trailer.ProcessStatus.LOADING) {
                trailer.setProcessStatus(Trailer.ProcessStatus.LOADED);
                trailer.setLoadStatus(Trailer.LoadStatus.FULL);
            } else if (trailer.getProcessStatus() == Trailer.ProcessStatus.UNLOADING) {
                trailer.setProcessStatus(Trailer.ProcessStatus.UNLOADED);
                trailer.setLoadStatus(Trailer.LoadStatus.EMPTY);
            }
            
            trailerRepository.save(trailer);
        }
        
        return moveRequestRepository.save(moveRequest);
    }

    @Override
    @Transactional
    public MoveRequest cancelMoveRequest(Long moveRequestId) {
        MoveRequest moveRequest = moveRequestRepository.findById(moveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Move request not found with id: " + moveRequestId));
        
        // Can only cancel if not already completed
        if (moveRequest.getStatus() == MoveRequest.MoveStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel a completed move request");
        }
        
        // Update status
        moveRequest.setStatus(MoveRequest.MoveStatus.CANCELLED);
        
        return moveRequestRepository.save(moveRequest);
    }

    @Override
    public List<MoveRequest> getPendingMoveRequests() {
        return moveRequestRepository.findByStatus(MoveRequest.MoveStatus.REQUESTED);
    }

    @Override
    public List<MoveRequest> getMoveRequestsBySpotterId(Long spotterId) {
        User spotter = userRepository.findById(spotterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + spotterId));
        
        return moveRequestRepository.findByAssignedSpotter(spotter);
    }

    @Override
    public MoveRequest getMoveRequestById(Long id) {
        return moveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Move request not found with id: " + id));
    }

    @Override
    public List<MoveRequest> getMoveRequestsBySite(Site site) {
        return moveRequestRepository.findBySite(site);
    }

    @Override
    public List<MoveRequest> getPendingMoveRequestsBySite(Site site) {
        return moveRequestRepository.findBySiteAndStatus(site, MoveRequest.MoveStatus.REQUESTED);
    }
}