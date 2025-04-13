package com.yardflowpro.repository;

import com.yardflowpro.model.Appointment;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.Trailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findBySite(Site site);
    List<Appointment> findByTrailer(Trailer trailer);
    List<Appointment> findByStatusAndScheduledTimeBetween(
        Appointment.AppointmentStatus status, 
        LocalDateTime start, 
        LocalDateTime end
    );
}