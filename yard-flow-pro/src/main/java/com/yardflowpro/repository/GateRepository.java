package com.yardflowpro.repository;

import com.yardflowpro.model.Gate;
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
 * Repository for managing {@link Gate} entities.
 * 
 * <p>Provides methods to search and retrieve gates based on various
 * criteria such as site, function, and code.</p>
 */
@Repository
public interface GateRepository extends JpaRepository<Gate, Long> {
    
    /**
     * Finds all gates at a specific site.
     *
     * @param site the site to find gates for
     * @return list of gates at the specified site
     */
    List<Gate> findBySite(Site site);
    
    /**
     * Finds a gate by its unique code.
     *
     * @param code the gate code
     * @return an Optional containing the gate if found
     */
    Optional<Gate> findByCode(String code);
    
    /**
     * Finds gates with a specific function.
     *
     * @param function the gate function to filter by
     * @return list of gates with the specified function
     */
    List<Gate> findByFunction(Gate.GateFunction function);
    
    /**
     * Finds gates at a specific site with a specific function.
     *
     * @param site the site to find gates for
     * @param function the gate function to filter by
     * @return list of gates matching both criteria
     */
    List<Gate> findBySiteAndFunction(Site site, Gate.GateFunction function);
    
    /**
     * Finds gates that support check-in operations at a specific site.
     *
     * @param site the site to find gates for
     * @return list of gates that support check-in at the specified site
     */
    @Query("SELECT g FROM Gate g WHERE g.site = :site AND " +
           "(g.function = com.yardflowpro.model.Gate$GateFunction.CHECK_IN OR " +
           "g.function = com.yardflowpro.model.Gate$GateFunction.CHECK_IN_OUT)")
    List<Gate> findCheckInGatesBySite(@Param("site") Site site);
    
    /**
     * Finds gates that support check-out operations at a specific site.
     *
     * @param site the site to find gates for
     * @return list of gates that support check-out at the specified site
     */
    @Query("SELECT g FROM Gate g WHERE g.site = :site AND " +
           "(g.function = com.yardflowpro.model.Gate$GateFunction.CHECK_OUT OR " +
           "g.function = com.yardflowpro.model.Gate$GateFunction.CHECK_IN_OUT)")
    List<Gate> findCheckOutGatesBySite(@Param("site") Site site);
    
    /**
     * Finds gates by name containing the given string (case-insensitive).
     *
     * @param name the name fragment to search for
     * @return list of gates matching the name fragment
     */
    @Query("SELECT g FROM Gate g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Gate> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Finds gates at a specific site with pagination.
     *
     * @param site the site to find gates for
     * @param pageable pagination information
     * @return page of gates at the specified site
     */
    Page<Gate> findBySite(Site site, Pageable pageable);
}