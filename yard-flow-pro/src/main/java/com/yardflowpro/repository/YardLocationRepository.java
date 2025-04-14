package com.yardflowpro.repository;

import com.yardflowpro.model.Site;
import com.yardflowpro.model.YardLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link YardLocation} entities.
 * 
 * <p>Provides methods to search and retrieve yard locations based on various
 * criteria such as site, status, and occupancy.</p>
 */
@Repository
public interface YardLocationRepository extends JpaRepository<YardLocation, Long> {
    
    /**
     * Finds all yard locations at a specific site.
     *
     * @param site the site to find yard locations for
     * @return list of yard locations at the specified site
     */
    List<YardLocation> findBySite(Site site);
    
    /**
     * Finds yard locations with a specific status.
     *
     * @param status the yard location status to filter by
     * @return list of yard locations with the specified status
     */
    List<YardLocation> findByStatus(YardLocation.LocationStatus status);
    
    /**
     * Finds yard locations by code.
     *
     * @param code the yard location code
     * @return an Optional containing the yard location if found
     */
    Optional<YardLocation> findByCode(String code);
    
    /**
     * Finds yard locations at a specific site with a specific status.
     *
     * @param site the site to find yard locations for
     * @param status the yard location status to filter by
     * @return list of yard locations matching both criteria
     */
    List<YardLocation> findBySiteAndStatus(Site site, YardLocation.LocationStatus status);
    
    /**
     * Finds yard locations that are available (not occupied) at a specific site.
     *
     * @param site the site to find available yard locations for
     * @return list of available yard locations at the specified site
     */
    @Query("SELECT y FROM YardLocation y WHERE y.site = :site AND y.status = com.yardflowpro.model.YardLocation$LocationStatus.AVAILABLE")
    List<YardLocation> findAvailableBySite(@Param("site") Site site);
    
    /**
     * Finds yard locations that are occupied at a specific site.
     *
     * @param site the site to find occupied yard locations for
     * @return list of occupied yard locations at the specified site
     */
    @Query("SELECT y FROM YardLocation y WHERE y.site = :site AND y.status = com.yardflowpro.model.YardLocation$LocationStatus.OCCUPIED")
    List<YardLocation> findOccupiedBySite(@Param("site") Site site);
    
    /**
     * Finds yard locations by name containing the given string (case-insensitive).
     *
     * @param name the name fragment to search for
     * @return list of yard locations matching the name fragment
     */
    @Query("SELECT y FROM YardLocation y WHERE LOWER(y.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<YardLocation> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Finds yard locations at a specific site with pagination.
     *
     * @param site the site to find yard locations for
     * @param pageable pagination information
     * @return page of yard locations at the specified site
     */
    Page<YardLocation> findBySite(Site site, Pageable pageable);
}