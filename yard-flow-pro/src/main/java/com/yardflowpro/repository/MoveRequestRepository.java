package com.yardflowpro.repository;

import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.Trailer;
import com.yardflowpro.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing {@link MoveRequest} entities.
 * 
 * <p>Provides methods to search and retrieve move requests based on various
 * criteria such as status, trailer, spotter, and site.</p>
 */
@Repository
public interface MoveRequestRepository extends JpaRepository<MoveRequest, Long> {
    
    /**
     * Finds move requests with a specific status.
     *
     * @param status the move request status to filter by
     * @return list of move requests with the specified status
     */
    List<MoveRequest> findByStatus(MoveRequest.MoveStatus status);
    
    /**
     * Finds move requests for a specific trailer.
     *
     * @param trailer the trailer to find move requests for
     * @return list of move requests for the specified trailer
     */
    List<MoveRequest> findByTrailer(Trailer trailer);
    
    /**
     * Finds move requests assigned to a specific spotter.
     *
     * @param spotter the spotter assigned to the move requests
     * @return list of move requests assigned to the specified spotter
     */
    List<MoveRequest> findByAssignedSpotter(User spotter);
    
    /**
     * Finds move requests at a specific site.
     *
     * @param site the site to find move requests for
     * @return list of move requests at the specified site
     */
    List<MoveRequest> findBySite(Site site);
    
    /**
     * Finds move requests at a specific site with a specific status.
     *
     * @param site the site to find move requests for
     * @param status the move request status to filter by
     * @return list of move requests matching both criteria
     */
    List<MoveRequest> findBySiteAndStatus(Site site, MoveRequest.MoveStatus status);
    
    /**
     * Finds move requests assigned to a specific spotter at a specific site.
     *
     * @param spotter the spotter assigned to the move requests
     * @param site the site to find move requests for
     * @return list of move requests matching both criteria
     */
    List<MoveRequest> findByAssignedSpotterAndSite(User spotter, Site site);
    
    /**
     * Finds move requests for a specific trailer with a specific status.
     *
     * @param trailer the trailer to find move requests for
     * @param status the move request status to filter by
     * @return list of move requests matching both criteria
     */
    List<MoveRequest> findByTrailerAndStatus(Trailer trailer, MoveRequest.MoveStatus status);
    
    /**
     * Finds move requests assigned to a specific spotter with a specific status.
     *
     * @param spotter the spotter assigned to the move requests
     * @param status the move request status to filter by
     * @return list of move requests matching both criteria
     */
    List<MoveRequest> findByAssignedSpotterAndStatus(User spotter, MoveRequest.MoveStatus status);
    
    /**
     * Finds move requests requested within a time range.
     *
     * @param start the start of the time range
     * @param end the end of the time range
     * @return list of move requests requested within the specified time range
     */
    List<MoveRequest> findByRequestTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Finds active move requests (requested, assigned, or in progress).
     *
     * @return list of active move requests
     */
    @Query("SELECT m FROM MoveRequest m WHERE m.status IN " +
           "(com.yardflowpro.model.MoveRequest$MoveStatus.REQUESTED, " +
           "com.yardflowpro.model.MoveRequest$MoveStatus.ASSIGNED, " +
           "com.yardflowpro.model.MoveRequest$MoveStatus.IN_PROGRESS)")
    List<MoveRequest> findActiveMoveRequests();
    
    /**
     * Finds active move requests at a specific site.
     *
     * @param site the site to find move requests for
     * @return list of active move requests at the specified site
     */
    @Query("SELECT m FROM MoveRequest m WHERE m.site = :site AND m.status IN " +
           "(com.yardflowpro.model.MoveRequest$MoveStatus.REQUESTED, " +
           "com.yardflowpro.model.MoveRequest$MoveStatus.ASSIGNED, " +
           "com.yardflowpro.model.MoveRequest$MoveStatus.IN_PROGRESS)")
    List<MoveRequest> findActiveMoveRequestsBySite(@Param("site") Site site);
    
    /**
     * Finds move requests at a specific site with pagination.
     *
     * @param site the site to find move requests for
     * @param pageable pagination information
     * @return page of move requests at the specified site
     */
    Page<MoveRequest> findBySite(Site site, Pageable pageable);
}