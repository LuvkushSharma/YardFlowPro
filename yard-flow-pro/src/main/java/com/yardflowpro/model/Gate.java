package com.yardflowpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
 * Represents a physical gate at a site where trailers enter or exit.
 * Gates control access to the facility and are the points where check-in
 * and check-out processes occur.
 */
@Entity
@Table(name = "gates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GateFunction function;
    
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    @JsonBackReference
    private Site site;
    
    @OneToMany(mappedBy = "checkInGate")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Appointment> checkInAppointments = new ArrayList<>();
    
    @OneToMany(mappedBy = "checkOutGate")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Appointment> checkOutAppointments = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Types of functions a gate can perform.
     */
    public enum GateFunction {
        /** Gate can only be used for check-in operations */
        CHECK_IN,
        
        /** Gate can only be used for check-out operations */
        CHECK_OUT,
        
        /** Gate can be used for both check-in and check-out operations */
        CHECK_IN_OUT
    }
}