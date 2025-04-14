package com.yardflowpro.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
 * Represents a specific location in the yard where trailers can be parked.
 * Yard locations are designated parking spaces for trailers when they're
 * not at a dock door.
 */
@Entity
@Table(name = "yard_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class YardLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationStatus status = LocationStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    @JsonIgnoreProperties("yardLocations")
    private Site site;

    @OneToOne(mappedBy = "yardLocation")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"assignedDoor", "yardLocation", "currentAppointment"})
    private Trailer currentTrailer;

    // Coordinates for yard map visualization
    private Double positionX;
    private Double positionY;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Status values representing the current state of a yard location.
     */
    public enum LocationStatus {
        /** Location is available for trailer parking */
        AVAILABLE,
        
        /** Location is currently occupied by a trailer */
        OCCUPIED,
        
        /** Location is not available for use (construction, etc.) */
        OUT_OF_SERVICE
    }
}