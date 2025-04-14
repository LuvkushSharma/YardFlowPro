package com.yardflowpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a request to move a trailer from one location to another within a site.
 * Move requests are typically assigned to spotters who physically move the trailers.
 */
@Entity
@Table(name = "move_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoveType moveType;
    
    @ManyToOne
    @JoinColumn(name = "trailer_id", nullable = false)
    private Trailer trailer;
    
    @Column(name = "source_location_type", nullable = false)
    private String sourceLocationType; // "YARD", "DOOR", "GATE"
    
    private Long sourceLocationId;
    
    @Column(name = "destination_location_type", nullable = false)
    private String destinationLocationType; // "YARD", "DOOR", "GATE"
    
    private Long destinationLocationId;
    
    @ManyToOne
    @JoinColumn(name = "assigned_spotter_id")
    private User assignedSpotter;
    
    private LocalDateTime requestTime;
    private LocalDateTime assignedTime;
    private LocalDateTime startTime;
    private LocalDateTime completionTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoveStatus status = MoveStatus.REQUESTED;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Types of move operations.
     */
    public enum MoveType {
        /** Moving a trailer to a door (for loading/unloading) */
        SPOT, 
        
        /** Moving a trailer from a door (after loading/unloading) */
        PULL  
    }
    
    /**
     * Status values representing the current state of a move request.
     */
    public enum MoveStatus {
        /** Move has been requested but not assigned */
        REQUESTED,
        
        /** Move has been assigned to a spotter */
        ASSIGNED,
        
        /** Move is currently being executed */
        IN_PROGRESS,
        
        /** Move has been completed */
        COMPLETED,
        
        /** Move has been cancelled */
        CANCELLED
    }
}