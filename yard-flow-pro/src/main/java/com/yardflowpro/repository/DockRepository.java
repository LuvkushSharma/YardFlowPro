package com.yardflowpro.repository;

import com.yardflowpro.model.Dock;
import com.yardflowpro.model.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Dock} entities.
 * 
 * <p>Provides methods to search and retrieve docks based on various
 * criteria such as site, code, and door availability.</p>
 */
@Repository
public interface DockRepository extends JpaRepository<Dock, Long> {
    
    /**
     * Finds all docks at a specific site.
     *
     * @param site the site to find docks for
     * @return list of docks at the specified site
     */
    List<Dock> findBySite(Site site);
    
    /**
     * Finds docks by code.
     *
     * @param code the dock code
     * @return list of docks matching the code
     */
    List<Dock> findByCode(String code);
    
    /**
     * Finds a unique dock by code.
     *
     * @param code the dock code
     * @return an Optional containing the dock if found
     */
    Optional<Dock> findFirstByCode(String code);
    
    /**
     * Finds docks by name containing the given string (case-insensitive).
     *
     * @param name the name fragment to search for
     * @return list of docks matching the name fragment
     */
    @Query("SELECT d FROM Dock d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Dock> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Finds docks that have at least one available door.
     *
     * @param site the site to find docks for
     * @return list of docks with at least one available door
     */
    @Query("SELECT DISTINCT d FROM Dock d JOIN d.doors door WHERE d.site = :site " +
           "AND door.status = com.yardflowpro.model.Door$DoorStatus.AVAILABLE")
    List<Dock> findWithAvailableDoors(@Param("site") Site site);
    
    /**
     * Finds docks at a specific site with pagination.
     *
     * @param site the site to find docks for
     * @param pageable pagination information
     * @return page of docks at the specified site
     */
    Page<Dock> findBySite(Site site, Pageable pageable);
}