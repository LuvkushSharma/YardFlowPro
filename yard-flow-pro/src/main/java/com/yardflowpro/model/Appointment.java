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
    @JoinColumn(name = "site_id")
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
    private AppointmentType type;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
    
    private LocalDateTime scheduledTime;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime completionTime;
    private String driverInfo;
    private String guardComments;
    
    public enum AppointmentType {
        LIVE_LOAD,
        DROP_AND_HOOK,
        INBOUND_ONLY,
        OUTBOUND_ONLY,
        CHECK_IN_ONLY,
        UNDEFINED
    }
    
    public enum AppointmentStatus {
        SCHEDULED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED
    }
}