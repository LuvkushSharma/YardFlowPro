package com.yardflowpro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents an appointment for trailer check-in/check-out operations.
 * An appointment tracks the lifecycle of a trailer's visit to a site,
 * including check-in, processing, and check-out.
 */
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "trailer_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Trailer trailer;
    
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    @JsonIgnore
    private Site site;
    
    @ManyToOne
    @JoinColumn(name = "check_in_gate_id")
    @JsonIgnore
    private Gate checkInGate;
    
    @ManyToOne
    @JoinColumn(name = "check_out_gate_id")
    @JsonIgnore
    private Gate checkOutGate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;
    
    private LocalDateTime scheduledTime;
    
    private LocalDateTime actualArrivalTime;
    
    private LocalDateTime completionTime;
    
    @Column(length = 500)
    private String driverInfo;
    
    @Column(columnDefinition = "TEXT")
    private String guardComments;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Types of appointments that can be scheduled.
     */
    public enum AppointmentType {
        /** Load trailer while driver waits */
        LIVE_LOAD,
        
        /** Exchange one trailer for another */
        DROP_AND_HOOK,
        
        /** Delivery only, no pickup */
        INBOUND_ONLY,
        
        /** Pickup only, no delivery */
        OUTBOUND_ONLY,
        
        /** Only checking in a trailer with no processing */
        CHECK_IN_ONLY,
        
        /** Default type when specific operation is not defined */
        UNDEFINED
    }
    
    /**
     * Status values representing the current state of an appointment.
     */
    public enum AppointmentStatus {
        /** Appointment is scheduled but trailer has not arrived */
        SCHEDULED, 
        
        /** Trailer has arrived and checked in through a gate */
        CHECKED_IN, 
        
        /** Trailer is being loaded, unloaded, or processed */
        IN_PROGRESS, 
        
        /** Appointment has been completed and trailer has departed */
        COMPLETED, 
        
        /** Appointment was cancelled and will not be completed */
        CANCELLED
    }
}