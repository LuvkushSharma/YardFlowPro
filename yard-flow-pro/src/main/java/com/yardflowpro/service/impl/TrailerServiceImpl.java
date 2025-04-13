package com.yardflowpro.service.impl;

import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Door;
import com.yardflowpro.model.Trailer;
import com.yardflowpro.model.YardLocation;
import com.yardflowpro.repository.DoorRepository;
import com.yardflowpro.repository.TrailerRepository;
import com.yardflowpro.repository.YardLocationRepository;
import com.yardflowpro.service.TrailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrailerServiceImpl implements TrailerService {

    private final TrailerRepository trailerRepository;
    private final DoorRepository doorRepository;
    private final YardLocationRepository yardLocationRepository;

    @Autowired
    public TrailerServiceImpl(
            TrailerRepository trailerRepository,
            DoorRepository doorRepository,
            YardLocationRepository yardLocationRepository) {
        this.trailerRepository = trailerRepository;
        this.doorRepository = doorRepository;
        this.yardLocationRepository = yardLocationRepository;
    }

    @Override
    public Trailer getTrailerById(Long id) {
        return trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + id));
    }

    @Override
    public Trailer getTrailerByNumber(String trailerNumber) {
        return trailerRepository.findByTrailerNumber(trailerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with number: " + trailerNumber));
    }

    @Override
    public List<Trailer> getTrailersBySiteId(Long siteId) {
        // This implementation filters trailers by their location (door or yard location)
        return trailerRepository.findAll().stream()
                .filter(trailer -> {
                    // Check if the trailer is at a door in the requested site
                    if (trailer.getAssignedDoor() != null) {
                        return trailer.getAssignedDoor().getDock().getSite().getId().equals(siteId);
                    } 
                    // Check if the trailer is at a yard location in the requested site
                    else if (trailer.getYardLocation() != null) {
                        return trailer.getYardLocation().getSite().getId().equals(siteId);
                    }
                    // Check if the trailer has an appointment at the requested site
                    else if (trailer.getCurrentAppointment() != null) {
                        return trailer.getCurrentAppointment().getSite().getId().equals(siteId);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Trailer> getTrailersByStatus(Trailer.ProcessStatus status) {
        return trailerRepository.findByProcessStatus(status);
    }

    @Override
    @Transactional
    public Trailer updateTrailerStatus(Long id, Trailer.ProcessStatus newStatus) {
        Trailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + id));
        
        // Apply business rules
        validateStatusTransition(trailer, newStatus);
        
        // Update the status
        trailer.setProcessStatus(newStatus);
        
        return trailerRepository.save(trailer);
    }

    @Override
    @Transactional
    public Trailer assignTrailerToDoor(Long trailerId, Long doorId) {
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + trailerId));
        
        Door door = doorRepository.findById(doorId)
                .orElseThrow(() -> new ResourceNotFoundException("Door not found with id: " + doorId));
        
        // Check if the door is available
        if (door.getStatus() != Door.DoorStatus.AVAILABLE) {
            throw new InvalidOperationException("Door is not available");
        }
        
        // If trailer is currently at a yard location, free it
        if (trailer.getYardLocation() != null) {
            YardLocation currentLocation = trailer.getYardLocation();
            currentLocation.setStatus(YardLocation.LocationStatus.AVAILABLE);
            currentLocation.setCurrentTrailer(null);
            yardLocationRepository.save(currentLocation);
            trailer.setYardLocation(null);
        }
        
        // If trailer is at another door, free it
        if (trailer.getAssignedDoor() != null && !trailer.getAssignedDoor().getId().equals(doorId)) {
            Door currentDoor = trailer.getAssignedDoor();
            currentDoor.setStatus(Door.DoorStatus.AVAILABLE);
            currentDoor.setCurrentTrailer(null);
            doorRepository.save(currentDoor);
        }
        
        // Assign trailer to door
        trailer.setAssignedDoor(door);
        door.setCurrentTrailer(trailer);
        door.setStatus(Door.DoorStatus.OCCUPIED);
        doorRepository.save(door);
        
        // Apply automation rules based on load status when assigned to a door
        if (trailer.getLoadStatus() == Trailer.LoadStatus.EMPTY) {
            trailer.setProcessStatus(Trailer.ProcessStatus.LOADING);
        } else if (trailer.getLoadStatus() == Trailer.LoadStatus.FULL) {
            trailer.setProcessStatus(Trailer.ProcessStatus.UNLOADING);
        }
        
        return trailerRepository.save(trailer);
    }

    @Override
    @Transactional
    public Trailer assignTrailerToYardLocation(Long trailerId, Long yardLocationId) {
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + trailerId));
        
        YardLocation yardLocation = yardLocationRepository.findById(yardLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Yard location not found with id: " + yardLocationId));
        
        // Check if the yard location is available
        if (yardLocation.getStatus() != YardLocation.LocationStatus.AVAILABLE) {
            throw new InvalidOperationException("Yard location is not available");
        }
        
        // If trailer is currently at a door, free it
        if (trailer.getAssignedDoor() != null) {
            Door currentDoor = trailer.getAssignedDoor();
            currentDoor.setStatus(Door.DoorStatus.AVAILABLE);
            currentDoor.setCurrentTrailer(null);
            doorRepository.save(currentDoor);
            trailer.setAssignedDoor(null);
        }
        
        // If trailer is at another yard location, free it
        if (trailer.getYardLocation() != null && !trailer.getYardLocation().getId().equals(yardLocationId)) {
            YardLocation currentLocation = trailer.getYardLocation();
            currentLocation.setStatus(YardLocation.LocationStatus.AVAILABLE);
            currentLocation.setCurrentTrailer(null);
            yardLocationRepository.save(currentLocation);
        }
        
        // Assign trailer to yard location
        trailer.setYardLocation(yardLocation);
        yardLocation.setCurrentTrailer(trailer);
        yardLocation.setStatus(YardLocation.LocationStatus.OCCUPIED);
        yardLocationRepository.save(yardLocation);
        
        return trailerRepository.save(trailer);
    }

    @Override
    public void updateDetentionStatus(Long trailerId) {
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + trailerId));
        
        // Skip if carrier does not have detention enabled
        if (trailer.getCarrier() == null || !trailer.getCarrier().isDetentionEnabled()) {
            return;
        }
        
        // Calculate time since check-in
        if (trailer.getCheckInTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration durationSinceCheckIn = Duration.between(trailer.getCheckInTime(), now);
            long hoursInYard = durationSinceCheckIn.toHours();
            
            // Start detention if free time has elapsed
            if (hoursInYard > trailer.getCarrier().getFreeTimeHours() && !trailer.isDetentionActive()) {
                trailer.setDetentionStartTime(trailer.getCheckInTime().plusHours(trailer.getCarrier().getFreeTimeHours()));
                trailer.setDetentionActive(true);
                trailerRepository.save(trailer);
            }
        }
    }
    
    private void validateStatusTransition(Trailer trailer, Trailer.ProcessStatus newStatus) {
        Trailer.LoadStatus loadStatus = trailer.getLoadStatus();
        
        // Rule: Can't mark a FULL trailer as LOAD, LOADING, or LOADED
        if (loadStatus == Trailer.LoadStatus.FULL && 
                (newStatus == Trailer.ProcessStatus.LOAD || 
                 newStatus == Trailer.ProcessStatus.LOADING || 
                 newStatus == Trailer.ProcessStatus.LOADED)) {
            throw new InvalidOperationException("Cannot change a FULL trailer to " + newStatus);
        }
        
        // Rule: Can't mark an EMPTY trailer as UNLOAD, UNLOADING, or UNLOADED
        if (loadStatus == Trailer.LoadStatus.EMPTY && 
                (newStatus == Trailer.ProcessStatus.UNLOAD || 
                 newStatus == Trailer.ProcessStatus.UNLOADING || 
                 newStatus == Trailer.ProcessStatus.UNLOADED)) {
            throw new InvalidOperationException("Cannot change an EMPTY trailer to " + newStatus);
        }
    }
}