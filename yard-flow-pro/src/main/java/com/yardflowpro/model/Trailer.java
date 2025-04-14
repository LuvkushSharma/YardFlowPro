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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trailer that can be checked in, moved, loaded/unloaded, and checked out.
 * Trailers are the primary assets managed in the yard management system.
 */
@Entity
@Table(name = "trailers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trailerNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoadStatus loadStatus = LoadStatus.EMPTY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessStatus processStatus = ProcessStatus.IN_GATE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrailerCondition condition = TrailerCondition.CLEAN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefrigerationStatus refrigerationStatus = RefrigerationStatus.NOT_APPLICABLE;

    @ManyToOne
    @JoinColumn(name = "carrier_id")
    @JsonIgnore
    private Carrier carrier;

    @OneToOne
    @JoinColumn(name = "door_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Door assignedDoor;

    @OneToOne
    @JoinColumn(name = "yard_location_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private YardLocation yardLocation;

    @OneToOne(mappedBy = "trailer")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Appointment currentAppointment;
    
    @OneToMany(mappedBy = "trailer")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Appointment> appointmentHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "trailer")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<MoveRequest> moveRequests = new ArrayList<>();

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // Detention tracking
    private LocalDateTime detentionStartTime;
    private boolean detentionActive;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Status values representing the load state of a trailer.
     */
    public enum LoadStatus {
        /** Trailer has no cargo */
        EMPTY,
        
        /** Trailer is partially loaded */
        PARTIAL,
        
        /** Trailer is fully loaded */
        FULL
    }

    /**
     * Status values representing the current processing state of a trailer.
     */
    public enum ProcessStatus {
        /** Trailer has just checked in */
        IN_GATE,
        
        /** Trailer needs to be loaded */
        LOAD,
        
        /** Trailer is currently being loaded */
        LOADING,
        
        /** Trailer has completed loading */
        LOADED,
        
        /** Trailer needs to be unloaded */
        UNLOAD,
        
        /** Trailer is currently being unloaded */
        UNLOADING,
        
        /** Trailer has completed unloading */
        UNLOADED
    }

    /**
     * Status values representing the physical condition of a trailer.
     */
    public enum TrailerCondition {
        /** Trailer is clean and in good condition */
        CLEAN,
        
        /** Trailer is dirty but usable */
        DIRTY,
        
        /** Trailer has damage that may need repair */
        DAMAGED
    }

    /**
     * Status values representing the refrigeration state of a trailer.
     */
    public enum RefrigerationStatus {
        /** Refrigeration is running and maintaining temperature */
        ACTIVE,
        
        /** Refrigeration is preparing to reach target temperature */
        PRE_COOLING,
        
        /** Refrigeration is in defrost cycle */
        DEFROST,
        
        /** Refrigeration is turned off */
        OFF,
        
        /** Trailer does not have refrigeration capability */
        NOT_APPLICABLE
    }
}