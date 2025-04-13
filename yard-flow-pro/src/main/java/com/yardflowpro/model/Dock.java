package com.yardflowpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "docks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String code;
    
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    @JsonBackReference
    private Site site;
    
    @OneToMany(mappedBy = "dock", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Door> doors = new HashSet<>();
}