package com.yardflowpro.repository;

import com.yardflowpro.model.Carrier;
import com.yardflowpro.model.Trailer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Trailer} entities.
 * 
 * <p>Provides methods to search and retrieve trailers based on various
 * criteria such as trailer number, carrier, status, and location.</p>
 */
@Repository
public interface TrailerRepository extends JpaRepository<Trailer, Long> {
    
    /**
     * Finds a trailer by its unique trailer number.
     *
     * @param trailerNumber the trailer number
     * @return an Optional containing the trailer if found
     */
    Optional<Trailer> findByTrailerNumber(String trailerNumber);
    
    /**
     * Finds trailers owned by a specific carrier.
     *
     * @param carrier the carrier that owns the trailers
     * @return list of trailers owned by the specified carrier
     */
    List<Trailer> findByCarrier(Carrier carrier);
    
    /**
     * Finds trailers with a specific process status.
     *
     * @param status the process status to filter by
     * @return list of trailers with the specified process status
     */
    List<Trailer> findByProcessStatus(Trailer.ProcessStatus status);
    
    /**
     * Finds trailers with active detention.
     *
     * @return list of trailers with active detention
     */
    @Query("SELECT t FROM Trailer t WHERE t.detentionActive = true")
    List<Trailer> findAllWithActiveDetention();
    
    /**
     * Finds trailers at a specific site (door, yard location, or appointment).
     *
     * @param siteId the ID of the site to find trailers at
     * @return list of trailers at the specified site
     */
    @Query("SELECT t FROM Trailer t WHERE " +
           "(t.assignedDoor.dock.site.id = :siteId) OR " +
           "(t.yardLocation.site.id = :siteId) OR " +
           "(t.currentAppointment.site.id = :siteId)")
    List<Trailer> findBySiteId(@Param("siteId") Long siteId);
    
    /**
     * Finds trailers by load status.
     *
     * @param loadStatus the load status to filter by
     * @return list of trailers with the specified load status
     */
    List<Trailer> findByLoadStatus(Trailer.LoadStatus loadStatus);
    
    /**
     * Finds trailers by trailer number containing the given string (case-insensitive).
     *
     * @param trailerNumber the trailer number fragment to search for
     * @return list of trailers matching the trailer number fragment
     */
    @Query("SELECT t FROM Trailer t WHERE LOWER(t.trailerNumber) LIKE LOWER(CONCAT('%', :trailerNumber, '%'))")
    List<Trailer> findByTrailerNumberContainingIgnoreCase(@Param("trailerNumber") String trailerNumber);
    
    /**
     * Finds trailers checked in within a time range.
     *
     * @param start the start of the time range
     * @param end the end of the time range
     * @return list of trailers checked in within the specified time range
     */
    List<Trailer> findByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Finds trailers that are currently at a door.
     *
     * @return list of trailers currently at a door
     */
    @Query("SELECT t FROM Trailer t WHERE t.assignedDoor IS NOT NULL")
    List<Trailer> findAllAtDoors();
    
    /**
     * Finds trailers that are currently at a yard location.
     *
     * @return list of trailers currently at a yard location
     */
    @Query("SELECT t FROM Trailer t WHERE t.yardLocation IS NOT NULL")
    List<Trailer> findAllAtYardLocations();
    
    /**
     * Finds trailers with pagination support.
     *
     * @param pageable pagination information
     * @return page of trailers
     */
    Page<Trailer> findAll(Pageable pageable);
    
    /**
     * Finds trailers by carrier with pagination.
     *
     * @param carrier the carrier that owns the trailers
     * @param pageable pagination information
     * @return page of trailers owned by the specified carrier
     */
    Page<Trailer> findByCarrier(Carrier carrier, Pageable pageable);
}