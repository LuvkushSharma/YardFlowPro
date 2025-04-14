package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for move request responses.
 * This DTO contains detailed information about a move request, including
 * related entities like site, trailer, and users involved.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequestResponseDto {
    private Long id;
    private String moveType;
    private String status;
    private String sourceLocationType;
    private Long sourceLocationId;
    private String destinationLocationType;
    private Long destinationLocationId;
    private String notes;
    
    // Timestamps
    private LocalDateTime requestTime;
    private LocalDateTime assignedTime;
    private LocalDateTime startTime;
    private LocalDateTime completionTime;
    
    // Site information
    private Long siteId;
    private String siteName;
    
    // Trailer information
    private Long trailerId;
    private String trailerNumber;
    
    // User information
    private Long requestedById;
    private String requestedByName;
    private Long assignedSpotterId;
    private String assignedSpotterName;
}