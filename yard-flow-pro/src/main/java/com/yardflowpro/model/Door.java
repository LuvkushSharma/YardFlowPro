package com.yardflowpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
 * Represents a physical door at a dock where trailers can be loaded or unloaded.
 * Doors are the specific locations where trailers dock for loading/unloading operations.
 */
@Entity
@Table(name = "doors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Door {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoorStatus status = DoorStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "dock_id", nullable = false)
    @JsonBackReference
    private Dock dock;

    @OneToOne(mappedBy = "assignedDoor")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Trailer currentTrailer;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Status values representing the current state of a door.
     */
    public enum DoorStatus {
        /** Door is available for trailer assignment */
        AVAILABLE,
        
        /** Door is currently occupied by a trailer */
        OCCUPIED,
        
        /** Door is not available for use (maintenance, etc.) */
        OUT_OF_SERVICE
    }
}