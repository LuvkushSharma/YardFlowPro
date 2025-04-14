package com.yardflowpro.repository;

import com.yardflowpro.model.Door;
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
 * Repository for managing {@link Door} entities.
 * 
 * <p>Provides methods to search and retrieve doors based on various
 * criteria such as dock, status, and site.</p>
 */
@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {
    
    /**
     * Finds all doors at a specific dock.
     *
     * @param dock the dock to find doors for
     * @return list of doors at the specified dock
     */
    List<Door> findByDock(Dock dock);
    
    /**
     * Finds doors with a specific status.
     *
     * @param status the door status to filter by
     * @return list of doors with the specified status
     */
    List<Door> findByStatus(Door.DoorStatus status);
    
    /**
     * Finds doors by their code.
     *
     * @param code the door code
     * @return an Optional containing the door if found
     */
    Optional<Door> findByCode(String code);
    
    /**
     * Finds doors by dock and status.
     *
     * @param dock the dock to find doors for
     * @param status the door status to filter by
     * @return list of doors matching both criteria
     */
    List<Door> findByDockAndStatus(Dock dock, Door.DoorStatus status);
    
    /**
     * Finds doors at a specific site.
     *
     * @param site the site to find doors for
     * @return list of doors at the specified site
     */
    @Query("SELECT d FROM Door d WHERE d.dock.site = :site")
    List<Door> findBySite(@Param("site") Site site);
    
    /**
     * Finds doors at a specific site with a given status.
     *
     * @param site the site to find doors for
     * @param status the door status to filter by
     * @return list of doors matching both criteria
     */
    @Query("SELECT d FROM Door d WHERE d.dock.site = :site AND d.status = :status")
    List<Door> findBySiteAndStatus(@Param("site") Site site, @Param("status") Door.DoorStatus status);
    
    /**
     * Finds doors with the specified status at any of the given docks.
     *
     * @param docks list of docks to search in
     * @param status the door status to filter by
     * @return list of doors matching the criteria
     */
    List<Door> findByDockInAndStatus(List<Dock> docks, Door.DoorStatus status);
    
    /**
     * Finds doors at a specific dock with pagination.
     *
     * @param dock the dock to find doors for
     * @param pageable pagination information
     * @return page of doors at the specified dock
     */
    Page<Door> findByDock(Dock dock, Pageable pageable);
}