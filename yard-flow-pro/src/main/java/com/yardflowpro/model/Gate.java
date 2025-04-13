package com.yardflowpro.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private GateFunction function;
    
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    @JsonBackReference
    private Site site;
    
    public enum GateFunction {
        CHECK_IN,
        CHECK_OUT,
        CHECK_IN_OUT
    }
}