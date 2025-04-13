package com.yardflowpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "site_id")
    private Site site;
    
    @Enumerated(EnumType.STRING)
    private MoveType moveType;
    
    @ManyToOne
    @JoinColumn(name = "trailer_id", nullable = false)
    private Trailer trailer;
    
    @Column(name = "source_location_type")
    private String sourceLocationType; // "YARD", "DOOR", "GATE"
    
    private Long sourceLocationId;
    
    @Column(name = "destination_location_type")
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
    private MoveStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne
    @JoinColumn(name = "requested_by_id")
    private User requestedBy;
    
    public enum MoveType {
        SPOT, // To door
        PULL  // From door
    }
    
    public enum MoveStatus {
        REQUESTED,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}