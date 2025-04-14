package com.yardflowpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a carrier/transportation company that delivers or picks up trailers.
 * Carriers can own tractors, trailers, or both, and may have detention fee configurations.
 */
@Entity
@Table(name = "carriers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private boolean ownsTractors;
    private boolean ownsTrailers;
    
    // Detention fee configuration
    private boolean detentionEnabled;
    
    @Column(name = "detention_starts_after_hours")
    private Integer detentionStartsAt; // Hours
    
    @Column(name = "detention_stops_after_hours")
    private Integer detentionStopsAt; // Hours
    
    @Column(name = "free_time_hours")
    private Integer freeTimeHours;
    
    @Column(name = "charge_interval_hours")
    private Integer chargeIntervalHours;
    
    @Column(name = "charge_per_interval", precision = 10, scale = 2)
    private BigDecimal chargePerInterval;
    
    @Column(name = "max_charge_enabled")
    private boolean maxChargeEnabled;
    
    @Column(name = "max_charge", precision = 10, scale = 2)
    private BigDecimal maxCharge;
    
    @ManyToMany
    @JoinTable(
        name = "carrier_site_eligibility",
        joinColumns = @JoinColumn(name = "carrier_id"),
        inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Site> eligibleSites = new HashSet<>();
    
    @OneToMany(mappedBy = "carrier")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Trailer> trailers = new HashSet<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}