package com.yardflowpro.model;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a user of the yard management system.
 * Users have different roles and can access specific sites.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @ManyToMany
    @JoinTable(
        name = "user_site_access",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Site> accessibleSites = new HashSet<>();
    
    @OneToMany(mappedBy = "assignedSpotter")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MoveRequest> assignedMoves = new ArrayList<>();
    
    @OneToMany(mappedBy = "requestedBy")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MoveRequest> requestedMoves = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Available user roles in the system.
     */
    public enum UserRole {
        /** Has full access to all system features and sites */
        SUPERUSER,
        
        /** Can manage users, configuration, and has broad access */
        ADMIN,
        
        /** Moves trailers within the yard and to/from doors */
        SPOTTER,
        
        /** Handles check-in and check-out processes at gates */
        GATE_GUARD,
        
        /** Manages loading/unloading operations at dock doors */
        DOCK_MANAGER
    }
    
    /**
     * Returns the full name of the user.
     * 
     * @return the user's first and last name combined
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}