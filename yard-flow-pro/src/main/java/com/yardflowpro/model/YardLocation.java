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

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private LocationStatus status;

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

    // Enum for location status, using proper naming conventions
    public enum LocationStatus {
        AVAILABLE,
        OCCUPIED,
        OUT_OF_SERVICE
    }
}
