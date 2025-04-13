package com.yardflowpro.service.impl;

import com.yardflowpro.dto.AppointmentDto;
import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.*;
import com.yardflowpro.repository.*;
import com.yardflowpro.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SiteRepository siteRepository;
    private final TrailerRepository trailerRepository;
    private final CarrierRepository carrierRepository;
    private final GateRepository gateRepository;

    @Autowired
    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            SiteRepository siteRepository,
            TrailerRepository trailerRepository,
            CarrierRepository carrierRepository,
            GateRepository gateRepository) {
        this.appointmentRepository = appointmentRepository;
        this.siteRepository = siteRepository;
        this.trailerRepository = trailerRepository;
        this.carrierRepository = carrierRepository;
        this.gateRepository = gateRepository;
    }

    @Override
    @Transactional
    public Appointment processCheckIn(CheckInRequestDto checkInRequest) {
        // Retrieve site
        Site site = siteRepository.findById(checkInRequest.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + checkInRequest.getSiteId()));

        // Retrieve gate
        Gate gate = gateRepository.findById(checkInRequest.getGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with id: " + checkInRequest.getGateId()));
        
        // Verify gate is at the same site
        if (!gate.getSite().getId().equals(site.getId())) {
            throw new InvalidOperationException("Gate does not belong to the specified site");
        }
        
        // Verify gate has CHECK_IN or CHECK_IN_OUT function
        if (gate.getFunction() != Gate.GateFunction.CHECK_IN && gate.getFunction() != Gate.GateFunction.CHECK_IN_OUT) {
            throw new InvalidOperationException("Selected gate does not support check-in operations");
        }

        // Retrieve carrier
        Carrier carrier = carrierRepository.findById(checkInRequest.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + checkInRequest.getCarrierId()));

        // Check if carrier is eligible for this site
        boolean isCarrierEligible = carrier.getEligibleSites().stream()
                .anyMatch(eligibleSite -> eligibleSite.getId().equals(site.getId()));
                
        if (!isCarrierEligible) {
            throw new InvalidOperationException(
                "Carrier " + carrier.getName() + " (ID: " + carrier.getId() + 
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
        appointment.setCheckInGate(gate);  // Set the gate used for check-in
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
        // Validate required fields
        if (checkOutRequest.getTrailerId() == null) {
            throw new InvalidOperationException("Trailer ID is required for check-out");
        }
        
        if (checkOutRequest.getGateId() == null) {
            throw new InvalidOperationException("Gate ID is required for check-out");
        }

        if (checkOutRequest.getSiteId() == null) {
            throw new InvalidOperationException("Site ID is required for check-out");
        }

        // Retrieve site
        Site site = siteRepository.findById(checkOutRequest.getSiteId())
        .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + checkOutRequest.getSiteId()));
        
        // Retrieve trailer
        Trailer trailer = trailerRepository.findById(checkOutRequest.getTrailerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + checkOutRequest.getTrailerId()));
                
        // Retrieve gate
        Gate gate = gateRepository.findById(checkOutRequest.getGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with id: " + checkOutRequest.getGateId()));
        
        // Retrieve the current appointment
        Appointment appointment = trailer.getCurrentAppointment();
        if (appointment == null) {
            throw new ResourceNotFoundException("No active appointment found for trailer id: " + trailer.getId());
        }
        
        // Verify site matches the appointment's site
        if (!appointment.getSite().getId().equals(checkOutRequest.getSiteId())) {
            throw new InvalidOperationException(
                "Site mismatch: Trailer was checked in at site " + appointment.getSite().getName() + 
                " (ID: " + appointment.getSite().getId() + "), but checkout was attempted at site " + 
                site.getName() + " (ID: " + site.getId() + ")"
            );
        }
        
        // Verify gate is at the same site as the appointment
        if (!gate.getSite().getId().equals(appointment.getSite().getId())) {
            throw new InvalidOperationException(
                "Gate belongs to site " + gate.getSite().getName() + 
                " (ID: " + gate.getSite().getId() + "), but trailer is at site " + 
                appointment.getSite().getName() + " (ID: " + appointment.getSite().getId() + ")"
            );
        }
        
        // Verify gate has CHECK_OUT or CHECK_IN_OUT function
        if (gate.getFunction() != Gate.GateFunction.CHECK_OUT && gate.getFunction() != Gate.GateFunction.CHECK_IN_OUT) {
            throw new InvalidOperationException("Selected gate does not support check-out operations");
        }
        
        // Update trailer details
        trailer.setCondition(checkOutRequest.getTrailerCondition());
        trailer.setLoadStatus(checkOutRequest.getLoadStatus());
        trailer.setCheckOutTime(LocalDateTime.now());
        
        // Set process status based on load status
        if (checkOutRequest.getLoadStatus() == Trailer.LoadStatus.EMPTY) {
            trailer.setProcessStatus(Trailer.ProcessStatus.UNLOADED);
        } else if (checkOutRequest.getLoadStatus() == Trailer.LoadStatus.FULL) {
            trailer.setProcessStatus(Trailer.ProcessStatus.LOADED);
        }
        
        // Handle door relationship
        if (trailer.getAssignedDoor() != null) {
            Door door = trailer.getAssignedDoor();
            door.setCurrentTrailer(null);
            door.setStatus(Door.DoorStatus.AVAILABLE);
            trailer.setAssignedDoor(null);
        }
        
        // Handle yard location relationship
        if (trailer.getYardLocation() != null) {
            YardLocation location = trailer.getYardLocation();
            location.setCurrentTrailer(null);
            location.setStatus(YardLocation.LocationStatus.AVAILABLE);
            trailer.setYardLocation(null);
        }
        
        // Stop detention timer if running
        if (trailer.isDetentionActive()) {
            trailer.setDetentionActive(false);
        }
        
        // Update appointment
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setCompletionTime(LocalDateTime.now());
        appointment.setCheckOutGate(gate);  // Set the gate used for check-out
        
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
        
        return appointmentRepository.findBySite(site).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CHECKED_IN || 
                             a.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS)
                .collect(Collectors.toList());
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
    
    @Override
    public List<Appointment> getAppointmentsByGateId(Long gateId) {
        Gate gate = gateRepository.findById(gateId)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with id: " + gateId));
        
        // In a real implementation, we'd need a repository method to find appointments by gate
        // For now, we'll do it the simple way by fetching all and filtering
        return appointmentRepository.findAll().stream()
                .filter(a -> (a.getCheckInGate() != null && a.getCheckInGate().getId().equals(gateId)) || 
                             (a.getCheckOutGate() != null && a.getCheckOutGate().getId().equals(gateId)))
                .collect(Collectors.toList());
    }
    
    @Override
    public AppointmentDto convertToDto(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setStatus(appointment.getStatus().name());
        dto.setAppointmentType(appointment.getType().name());
        dto.setScheduledTime(appointment.getScheduledTime());
        dto.setActualArrivalTime(appointment.getActualArrivalTime());
        dto.setCompletionTime(appointment.getCompletionTime());
        dto.setDriverInfo(appointment.getDriverInfo());
        dto.setGuardComments(appointment.getGuardComments());
        
        if (appointment.getSite() != null) {
            dto.setSiteId(appointment.getSite().getId());
            dto.setSiteName(appointment.getSite().getName());
        }
        
        if (appointment.getTrailer() != null) {
            dto.setTrailerId(appointment.getTrailer().getId());
            dto.setTrailerNumber(appointment.getTrailer().getTrailerNumber());
            
            if (appointment.getTrailer().getCarrier() != null) {
                dto.setCarrierId(appointment.getTrailer().getCarrier().getId());
                dto.setCarrierName(appointment.getTrailer().getCarrier().getName());
            }
        }
        
        if (appointment.getCheckInGate() != null) {
            dto.setCheckInGateId(appointment.getCheckInGate().getId());
            dto.setCheckInGateName(appointment.getCheckInGate().getName());
        }
        
        if (appointment.getCheckOutGate() != null) {
            dto.setCheckOutGateId(appointment.getCheckOutGate().getId());
            dto.setCheckOutGateName(appointment.getCheckOutGate().getName());
        }
        
        return dto;
    }
}