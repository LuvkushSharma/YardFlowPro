package com.yardflowpro.controller;

import com.yardflowpro.dto.TrailerDto;
import com.yardflowpro.model.Trailer;
import com.yardflowpro.service.TrailerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trailers")
public class TrailerController {

    private final TrailerService trailerService;

    @Autowired
    public TrailerController(TrailerService trailerService) {
        this.trailerService = trailerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrailerDto> getTrailerById(@PathVariable Long id) {
        Trailer trailer = trailerService.getTrailerById(id);
        return ResponseEntity.ok(convertToDto(trailer));
    }

    @GetMapping("/number/{trailerNumber}")
    public ResponseEntity<TrailerDto> getTrailerByNumber(@PathVariable String trailerNumber) {
        Trailer trailer = trailerService.getTrailerByNumber(trailerNumber);
        return ResponseEntity.ok(convertToDto(trailer));
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<TrailerDto>> getTrailersBySite(@PathVariable Long siteId) {
        List<TrailerDto> trailers = trailerService.getTrailersBySiteId(siteId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trailers);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TrailerDto>> getTrailersByStatus(@PathVariable Trailer.ProcessStatus status) {
        List<TrailerDto> trailers = trailerService.getTrailersByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trailers);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TrailerDto> updateTrailerStatus(
            @PathVariable Long id, 
            @RequestParam Trailer.ProcessStatus newStatus) {
        Trailer trailer = trailerService.updateTrailerStatus(id, newStatus);
        return ResponseEntity.ok(convertToDto(trailer));
    }

    @PatchMapping("/{id}/assign-door/{doorId}")
    public ResponseEntity<TrailerDto> assignTrailerToDoor(
            @PathVariable Long id, 
            @PathVariable Long doorId) {
        Trailer trailer = trailerService.assignTrailerToDoor(id, doorId);
        return ResponseEntity.ok(convertToDto(trailer));
    }

    @PatchMapping("/{id}/assign-yard-location/{locationId}")
    public ResponseEntity<TrailerDto> assignTrailerToYardLocation(
            @PathVariable Long id, 
            @PathVariable Long locationId) {
        Trailer trailer = trailerService.assignTrailerToYardLocation(id, locationId);
        return ResponseEntity.ok(convertToDto(trailer));
    }

    @PostMapping("/{id}/update-detention")
    public ResponseEntity<Void> updateDetentionStatus(@PathVariable Long id) {
        trailerService.updateDetentionStatus(id);
        return ResponseEntity.ok().build();
    }
    
    // Helper method to convert Trailer entity to TrailerDto
    private TrailerDto convertToDto(Trailer trailer) {
        TrailerDto dto = new TrailerDto();
        dto.setId(trailer.getId());
        dto.setTrailerNumber(trailer.getTrailerNumber());
        dto.setLoadStatus(trailer.getLoadStatus());
        dto.setTrailerCondition(trailer.getCondition());
        
        // Set refrigerated status
        dto.setRefrigerated(trailer.getRefrigerationStatus() != null && 
                trailer.getRefrigerationStatus() != Trailer.RefrigerationStatus.NOT_APPLICABLE);
        
        // Set location string
        String location = "";
        if (trailer.getAssignedDoor() != null) {
            location = "Door: " + trailer.getAssignedDoor().getName();
        } else if (trailer.getYardLocation() != null) {
            location = "Yard: " + trailer.getYardLocation().getName();
        }
        dto.setLocation(location);
        
        // Set carrier name
        if (trailer.getCarrier() != null) {
            dto.setCarrierName(trailer.getCarrier().getName());
        }
        
        // Set appointment status
        if (trailer.getCurrentAppointment() != null) {
            dto.setAppointmentStatus(trailer.getCurrentAppointment().getStatus().name());
        }
        
        return dto;
    }
}