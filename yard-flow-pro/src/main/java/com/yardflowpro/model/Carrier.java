package com.yardflowpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

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
    private Integer detentionStartsAt; // Hours
    private Integer detentionStopsAt; // Hours
    private Integer freeTimeHours;
    private Integer chargeIntervalHours;
    private BigDecimal chargePerInterval;
    private boolean maxChargeEnabled;
    private BigDecimal maxCharge;
    
    @ManyToMany
    @JoinTable(
        name = "carrier_site_eligibility",
        joinColumns = @JoinColumn(name = "carrier_id"),
        inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    private Set<Site> eligibleSites = new HashSet<>();
    
    @OneToMany(mappedBy = "carrier")
    private Set<Trailer> trailers = new HashSet<>();
}