package com.yardflowpro.repository;

import com.yardflowpro.model.Appointment;
import com.yardflowpro.model.Gate;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.Trailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findBySite(Site site);
    List<Appointment> findByTrailer(Trailer trailer);
    List<Appointment> findByCheckInGate(Gate gate);
    List<Appointment> findByCheckOutGate(Gate gate);
    
    @Query("SELECT a FROM Appointment a WHERE a.checkInGate.id = :gateId OR a.checkOutGate.id = :gateId")
    List<Appointment> findByGateId(@Param("gateId") Long gateId);
    
    List<Appointment> findByStatusAndScheduledTimeBetween(
        Appointment.AppointmentStatus status, 
        LocalDateTime start, 
        LocalDateTime end
    );
}