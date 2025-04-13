package com.yardflowpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    public enum DoorStatus {
        AVAILABLE,
        OCCUPIED,
        OUT_OF_SERVICE
    }
}