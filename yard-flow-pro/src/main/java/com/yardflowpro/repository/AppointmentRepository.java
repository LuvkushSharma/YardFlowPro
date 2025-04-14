package com.yardflowpro.repository;

import com.yardflowpro.model.Appointment;
import com.yardflowpro.model.Gate;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.Trailer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing {@link Appointment} entities.
 * 
 * <p>Provides methods to search and retrieve appointments based on various
 * criteria such as site, trailer, gate, and time ranges.</p>
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    /**
     * Finds all appointments at a specific site.
     *
     * @param site the site to find appointments for
     * @return list of appointments at the specified site
     */
    List<Appointment> findBySite(Site site);
    
    /**
     * Finds all appointments at a specific site with pagination.
     *
     * @param site the site to find appointments for
     * @param pageable pagination information
     * @return page of appointments at the specified site
     */
    Page<Appointment> findBySite(Site site, Pageable pageable);
    
    /**
     * Finds all appointments for a specific trailer.
     *
     * @param trailer the trailer to find appointments for
     * @return list of appointments for the specified trailer
     */
    List<Appointment> findByTrailer(Trailer trailer);
    
    /**
     * Finds all appointments that used a specific gate for check-in.
     *
     * @param gate the gate used for check-in
     * @return list of appointments that used the specified gate for check-in
     */
    List<Appointment> findByCheckInGate(Gate gate);
    
    /**
     * Finds all appointments that used a specific gate for check-out.
     *
     * @param gate the gate used for check-out
     * @return list of appointments that used the specified gate for check-out
     */
    List<Appointment> findByCheckOutGate(Gate gate);
    
    /**
     * Finds all appointments that used a specific gate for either check-in or check-out.
     *
     * @param gateId the ID of the gate
     * @return list of appointments that used the specified gate
     */
    @Query("SELECT a FROM Appointment a WHERE a.checkInGate.id = :gateId OR a.checkOutGate.id = :gateId")
    List<Appointment> findByGateId(@Param("gateId") Long gateId);
    
    /**
     * Finds appointments with a specific status and scheduled within a time range.
     *
     * @param status the appointment status to filter by
     * @param start the start of the time range
     * @param end the end of the time range
     * @return list of appointments matching the criteria
     */
    List<Appointment> findByStatusAndScheduledTimeBetween(
        Appointment.AppointmentStatus status, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    /**
     * Finds appointments with any of the specified statuses.
     *
     * @param statuses array of appointment statuses to include
     * @return list of appointments with any of the specified statuses
     */
    List<Appointment> findByStatusIn(Appointment.AppointmentStatus[] statuses);
    
    /**
     * Finds active appointments (checked in or in progress) at a specific site.
     *
     * @param site the site to find active appointments for
     * @return list of active appointments at the specified site
     */
    @Query("SELECT a FROM Appointment a WHERE a.site = :site AND " +
           "(a.status = com.yardflowpro.model.Appointment$AppointmentStatus.CHECKED_IN OR " +
           "a.status = com.yardflowpro.model.Appointment$AppointmentStatus.IN_PROGRESS)")
    List<Appointment> findActiveBySite(@Param("site") Site site);
    
    /**
     * Counts appointments by status at a specific site.
     *
     * @param site the site to count appointments for
     * @return array of objects containing status and count
     */
    @Query("SELECT a.status as status, COUNT(a) as count FROM Appointment a " +
           "WHERE a.site = :site GROUP BY a.status")
    List<Object[]> countByStatusForSite(@Param("site") Site site);
}