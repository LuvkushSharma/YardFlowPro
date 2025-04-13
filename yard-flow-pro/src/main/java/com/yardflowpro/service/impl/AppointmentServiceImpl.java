package com.yardflowpro.service.impl;

import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.*;
import com.yardflowpro.repository.AppointmentRepository;
import com.yardflowpro.repository.CarrierRepository;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.repository.TrailerRepository;
import com.yardflowpro.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SiteRepository siteRepository;
    private final TrailerRepository trailerRepository;
    private final CarrierRepository carrierRepository;

    @Autowired
    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            SiteRepository siteRepository,
            TrailerRepository trailerRepository,
            CarrierRepository carrierRepository) {
        this.appointmentRepository = appointmentRepository;
        this.siteRepository = siteRepository;
        this.trailerRepository = trailerRepository;
        this.carrierRepository = carrierRepository;
    }

    @Override
    @Transactional
    public Appointment processCheckIn(CheckInRequestDto checkInRequest) {
        // Retrieve site
        Site site = siteRepository.findById(checkInRequest.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + checkInRequest.getSiteId()));

        // Retrieve carrier
        Carrier carrier_detail = carrierRepository.findById(checkInRequest.getCarrierId())
        .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + checkInRequest.getCarrierId()));

        // Check if carrier is eligible for this site
        boolean isCarrierEligible = carrier_detail.getEligibleSites().stream()
                .anyMatch(eligibleSite -> eligibleSite.getId().equals(site.getId()));
                
        if (!isCarrierEligible) {
            throw new InvalidOperationException(
                "Carrier " + carrier_detail.getName() + " (ID: " + carrier_detail.getId() + 
                ") is not eligible for site " + site.getName() + " (ID: " + site.getId() + ")"
            );
        }

        // Find or create trailer
        Trailer trailer;
        Optional<Trailer> existingTrailer = trailerRepository.findByTrailerNumber(checkInRequest.getTrailerNumber());
        
        if (existingTrailer.isPresent()) {
            trailer = existingTrailer.get();
        } else {
            // Create new trailer
            trailer = new Trailer();
            trailer.setTrailerNumber(checkInRequest.getTrailerNumber());
        }
        
        // Set trailer properties
        Carrier carrier = carrierRepository.findById(checkInRequest.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + checkInRequest.getCarrierId()));
        
        trailer.setCarrier(carrier);
        trailer.setLoadStatus(checkInRequest.getLoadStatus());
        trailer.setCondition(checkInRequest.getTrailerCondition());
        trailer.setRefrigerationStatus(checkInRequest.getRefrigerationStatus());
        trailer.setCheckInTime(LocalDateTime.now());
        
        // Apply automation rules for process status
        if (trailer.getLoadStatus() == Trailer.LoadStatus.EMPTY) {
            trailer.setProcessStatus(Trailer.ProcessStatus.LOAD);
        } else if (trailer.getLoadStatus() == Trailer.LoadStatus.FULL) {
            trailer.setProcessStatus(Trailer.ProcessStatus.UNLOAD);
        } else {
            trailer.setProcessStatus(Trailer.ProcessStatus.IN_GATE);
        }
        
        // If detention is enabled for this carrier, start tracking free time
        if (carrier.isDetentionEnabled()) {
            trailer.setDetentionActive(false);  // Will be activated when free time expires
        }
        
        // Save trailer first to ensure it has an ID
        trailer = trailerRepository.save(trailer);
        
        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setSite(site);
        appointment.setTrailer(trailer);
        appointment.setType(checkInRequest.getAppointmentType());
        appointment.setScheduledTime(checkInRequest.getScheduledTime());
        appointment.setActualArrivalTime(LocalDateTime.now());
        appointment.setDriverInfo(checkInRequest.getDriverInfo());
        appointment.setGuardComments(checkInRequest.getGuardComments());
        appointment.setStatus(Appointment.AppointmentStatus.CHECKED_IN);
        
        // Set the bidirectional reference
        trailer.setCurrentAppointment(appointment);
        
        // Save appointment
        appointment = appointmentRepository.save(appointment);
        
        // Update trailer with the appointment reference
        trailerRepository.save(trailer);
        
        return appointment;
    }

    @Override
    @Transactional
    public Appointment processCheckOut(CheckOutRequestDto checkOutRequest) {
        // Retrieve trailer
        Trailer trailer = trailerRepository.findById(checkOutRequest.getTrailerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + checkOutRequest.getTrailerId()));
        
        // Update trailer details
        trailer.setCondition(checkOutRequest.getTrailerCondition());
        trailer.setLoadStatus(checkOutRequest.getLoadStatus());
        trailer.setCheckOutTime(LocalDateTime.now());
        
        // Handle door relationship
        if (trailer.getAssignedDoor() != null) {
            Door door = trailer.getAssignedDoor();
            door.setCurrentTrailer(null);
            door.setStatus(Door.DoorStatus.AVAILABLE);
            trailer.setAssignedDoor(null);
            // No need to explicitly save door here as it will be cascaded through trailer
        }
        
        // Handle yard location relationship
        if (trailer.getYardLocation() != null) {
            YardLocation location = trailer.getYardLocation();
            location.setCurrentTrailer(null);
            location.setStatus(YardLocation.LocationStatus.AVAILABLE);
            trailer.setYardLocation(null);
            // No need to explicitly save location here as it will be cascaded through trailer
        }
        
        // Stop detention timer if running
        if (trailer.isDetentionActive()) {
            trailer.setDetentionActive(false);
        }
        
        // Retrieve the current appointment (safe even with circular references due to annotations)
        Appointment appointment = trailer.getCurrentAppointment();
        if (appointment == null) {
            throw new ResourceNotFoundException("No active appointment found for trailer id: " + trailer.getId());
        }
        
        // Update appointment
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        if (checkOutRequest.getGuardComments() != null && !checkOutRequest.getGuardComments().isEmpty()) {
            if (appointment.getGuardComments() != null) {
                appointment.setGuardComments(appointment.getGuardComments() + 
                        "\nCheck-Out Comments: " + checkOutRequest.getGuardComments());
            } else {
                appointment.setGuardComments("Check-Out Comments: " + checkOutRequest.getGuardComments());
            }
        }
        
        // Break the bidirectional reference
        trailer.setCurrentAppointment(null);
        
        // Save trailer first to apply all relationship changes
        trailerRepository.save(trailer);
        
        // Then save appointment
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getActiveAppointments(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
        
        // Use repository method instead of stream filtering if available
        // Otherwise, use the current implementation
        return appointmentRepository.findBySite(site).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CHECKED_IN || 
                             a.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS)
                .toList();
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    @Override
    public List<Appointment> getAppointmentsByTrailerId(Long trailerId) {
        Trailer trailer = trailerRepository.findById(trailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + trailerId));
        
        return appointmentRepository.findByTrailer(trailer);
    }
}