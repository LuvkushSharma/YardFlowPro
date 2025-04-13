package com.yardflowpro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

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
    private LoadStatus loadStatus;

    @Enumerated(EnumType.STRING)
    private ProcessStatus processStatus;

    @Enumerated(EnumType.STRING)
    private TrailerCondition condition;

    @Enumerated(EnumType.STRING)
    private RefrigerationStatus refrigerationStatus;

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

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // Detention tracking
    private LocalDateTime detentionStartTime;
    private boolean detentionActive;

    // Enum for LoadStatus
    public enum LoadStatus {
        EMPTY,
        PARTIAL,
        FULL
    }

    // Enum for ProcessStatus
    public enum ProcessStatus {
        IN_GATE,
        LOAD,
        LOADING,
        LOADED,
        UNLOAD,
        UNLOADING,
        UNLOADED
    }

    // Enum for TrailerCondition
    public enum TrailerCondition {
        CLEAN,
        DIRTY,
        DAMAGED
    }

    // Enum for RefrigerationStatus
    public enum RefrigerationStatus {
        ACTIVE,
        PRE_COOLING,
        DEFROST,
        OFF,
        NOT_APPLICABLE
    }
}