package com.yardflowpro.service.impl;

import com.yardflowpro.dto.AppointmentDto;
import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.*;
import com.yardflowpro.repository.*;
import com.yardflowpro.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the AppointmentService interface for managing trailer appointments.
 * <p>
 * This implementation handles check-in, check-out, appointment scheduling, and
 * appointment lifecycle management operations.
 * </p>
 */
@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SiteRepository siteRepository;
    private final TrailerRepository trailerRepository;
    private final CarrierRepository carrierRepository;
    private final GateRepository gateRepository;

    /**
     * Creates a new AppointmentServiceImpl with required repositories.
     *
     * @param appointmentRepository repository for appointment entities
     * @param siteRepository repository for site entities
     * @param trailerRepository repository for trailer entities
     * @param carrierRepository repository for carrier entities
     * @param gateRepository repository for gate entities
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Appointment processCheckIn(CheckInRequestDto checkInRequest) {
        log.info("Processing check-in for trailer: {}, site: {}", 
                checkInRequest.getTrailerNumber(), checkInRequest.getSiteId());
        
        validateCheckInRequest(checkInRequest);
        
        // Retrieve and validate entities
        Site site = getSiteById(checkInRequest.getSiteId());
        Gate gate = getGateById(checkInRequest.getGateId());
        Carrier carrier = getCarrierById(checkInRequest.getCarrierId());

        validateGateForCheckIn(gate, site);
        validateCarrierEligibility(carrier, site);

        // Process trailer
        Trailer trailer = findOrCreateTrailer(checkInRequest);
        updateTrailerForCheckIn(trailer, carrier, checkInRequest);
        
        // Create appointment
        Appointment appointment = createCheckInAppointment(trailer, site, gate, checkInRequest);
        
        // Set bidirectional relationship
        trailer.setCurrentAppointment(appointment);
        
        // Save entities
        log.debug("Saving new appointment for trailer: {}", trailer.getTrailerNumber());
        appointment = appointmentRepository.save(appointment);
        trailerRepository.save(trailer);
        
        log.info("Check-in completed for trailer: {}, appointment ID: {}", 
                trailer.getTrailerNumber(), appointment.getId());
        return appointment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Appointment processCheckOut(CheckOutRequestDto checkOutRequest) {
        log.info("Processing check-out for trailer ID: {}, site: {}", 
                checkOutRequest.getTrailerId(), checkOutRequest.getSiteId());
        
        validateCheckOutRequest(checkOutRequest);

        // Retrieve and validate entities
        Site site = getSiteById(checkOutRequest.getSiteId());
        Trailer trailer = getTrailerById(checkOutRequest.getTrailerId());
        Gate gate = getGateById(checkOutRequest.getGateId());
        Appointment appointment = getActiveAppointmentForTrailer(trailer);

        validateGateForCheckOut(gate, site, appointment);

        // Update entities
        updateTrailerForCheckOut(trailer, checkOutRequest);
        releaseTrailerResources(trailer);
        updateAppointmentForCheckOut(appointment, gate, checkOutRequest);
        
        // Break bidirectional reference
        trailer.setCurrentAppointment(null);
        
        // Save entities
        trailerRepository.save(trailer);
        appointment = appointmentRepository.save(appointment);
        
        log.info("Check-out completed for trailer ID: {}, appointment ID: {}", 
                trailer.getId(), appointment.getId());
        return appointment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getActiveAppointments(Long siteId) {
        log.debug("Retrieving active appointments for site ID: {}", siteId);
        Site site = getSiteById(siteId);
        
        // Use optimized repository method if available
        if (supportsActiveAppointmentsQuery()) {
            return appointmentRepository.findActiveBySite(site);
        }
        
        // Fallback to manual filtering
        return appointmentRepository.findBySite(site).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CHECKED_IN || 
                             a.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        log.debug("Retrieving appointment with ID: {}", id);
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByTrailerId(Long trailerId) {
        log.debug("Retrieving appointments for trailer ID: {}", trailerId);
        Trailer trailer = getTrailerById(trailerId);
        return appointmentRepository.findByTrailer(trailer);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByGateId(Long gateId) {
        log.debug("Retrieving appointments for gate ID: {}", gateId);
        getGateById(gateId); // Validate gate exists
        
        // Use optimized repository method
        return appointmentRepository.findByGateId(gateId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsBySiteId(Long siteId) {
        log.debug("Retrieving all appointments for site ID: {}", siteId);
        Site site = getSiteById(siteId);
        return appointmentRepository.findBySite(site);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Retrieving appointments in date range: {} to {}", startDate, endDate);
        
        if (startDate == null || endDate == null) {
            throw new InvalidOperationException("Both start and end dates are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new InvalidOperationException("Start date cannot be after end date");
        }
        
        return appointmentRepository.findByStatusAndScheduledTimeBetween(
                Appointment.AppointmentStatus.SCHEDULED, startDate, endDate);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Appointment cancelAppointment(Long id, String cancelReason) {
        log.info("Cancelling appointment with ID: {}", id);
        
        Appointment appointment = getAppointmentById(id);
        
        // Validate appointment can be cancelled
        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel a completed appointment");
        }
        
        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new InvalidOperationException("Appointment is already cancelled");
        }
        
        // Update appointment status
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        
        // Add cancellation reason to comments
        if (cancelReason != null && !cancelReason.isEmpty()) {
            if (appointment.getGuardComments() != null && !appointment.getGuardComments().isEmpty()) {
                appointment.setGuardComments(appointment.getGuardComments() + 
                        "\nCancellation Reason: " + cancelReason);
            } else {
                appointment.setGuardComments("Cancellation Reason: " + cancelReason);
            }
        }
        
        // Handle trailer relationship if checked in
        if (appointment.getTrailer() != null && 
            appointment.getTrailer().getCurrentAppointment() != null &&
            appointment.getTrailer().getCurrentAppointment().getId().equals(appointment.getId())) {
            
            Trailer trailer = appointment.getTrailer();
            trailer.setCurrentAppointment(null);
            trailerRepository.save(trailer);
        }
        
        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        log.info("Successfully cancelled appointment with ID: {}", id);
        
        return cancelledAppointment;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Appointment startProcessingAppointment(Long id) {
        log.info("Starting processing for appointment with ID: {}", id);
        
        Appointment appointment = getAppointmentById(id);
        
        // Validate appointment can be processed
        if (appointment.getStatus() != Appointment.AppointmentStatus.CHECKED_IN) {
            throw new InvalidOperationException(
                    "Cannot start processing appointment with status: " + appointment.getStatus() + 
                    ". Appointment must be in CHECKED_IN status.");
        }
        
        // Update appointment status
        appointment.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);
        
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Successfully started processing appointment with ID: {}", id);
        
        return updatedAppointment;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Appointment scheduleAppointment(AppointmentDto appointmentDto) {
        log.info("Scheduling new appointment for trailer: {} at site: {}", 
                appointmentDto.getTrailerNumber(), appointmentDto.getSiteId());
        
        validateScheduledAppointment(appointmentDto);
        
        Appointment appointment = convertToEntity(appointmentDto);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        
        Appointment scheduledAppointment = appointmentRepository.save(appointment);
        log.info("Successfully scheduled appointment with ID: {}", scheduledAppointment.getId());
        
        return scheduledAppointment;
    }
    
    /**
     * {@inheritDoc}
     */
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
        
        mapSiteToDto(appointment, dto);
        mapTrailerToDto(appointment, dto);
        mapGatesToDto(appointment, dto);
        
        return dto;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Appointment convertToEntity(AppointmentDto appointmentDto) {
        Appointment appointment = new Appointment();
        
        if (appointmentDto.getId() != null) {
            appointment = appointmentRepository.findById(appointmentDto.getId())
                    .orElse(new Appointment());
        }
        
        // Set basic properties
        if (appointmentDto.getAppointmentType() != null) {
            appointment.setType(Appointment.AppointmentType.valueOf(appointmentDto.getAppointmentType()));
        }
        
        if (appointmentDto.getStatus() != null) {
            appointment.setStatus(Appointment.AppointmentStatus.valueOf(appointmentDto.getStatus()));
        } else {
            // Default for new appointments
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        }
        
        appointment.setScheduledTime(appointmentDto.getScheduledTime());
        appointment.setDriverInfo(appointmentDto.getDriverInfo());
        appointment.setGuardComments(appointmentDto.getGuardComments());
        
        // Set relationships
        if (appointmentDto.getSiteId() != null) {
            appointment.setSite(getSiteById(appointmentDto.getSiteId()));
        }
        
        if (appointmentDto.getTrailerId() != null) {
            appointment.setTrailer(getTrailerById(appointmentDto.getTrailerId()));
        } else if (appointmentDto.getTrailerNumber() != null) {
            // Try to find by trailer number
            Optional<Trailer> trailer = trailerRepository.findByTrailerNumber(appointmentDto.getTrailerNumber());
            trailer.ifPresent(appointment::setTrailer);
        }
        
        if (appointmentDto.getCheckInGateId() != null) {
            appointment.setCheckInGate(getGateById(appointmentDto.getCheckInGateId()));
        }
        
        if (appointmentDto.getCheckOutGateId() != null) {
            appointment.setCheckOutGate(getGateById(appointmentDto.getCheckOutGateId()));
        }
        
        return appointment;
    }

    // -------------------------------------------------------------------------
    // Private helper methods for validation
    // -------------------------------------------------------------------------
    
    /**
     * Validates check-in request data.
     */
    private void validateCheckInRequest(CheckInRequestDto request) {
        if (request.getSiteId() == null) {
            throw new InvalidOperationException("Site ID is required for check-in");
        }
        
        if (request.getGateId() == null) {
            throw new InvalidOperationException("Gate ID is required for check-in");
        }
        
        if (request.getTrailerNumber() == null || request.getTrailerNumber().trim().isEmpty()) {
            throw new InvalidOperationException("Trailer number is required for check-in");
        }
        
        if (request.getCarrierId() == null) {
            throw new InvalidOperationException("Carrier ID is required for check-in");
        }
        
        if (request.getLoadStatus() == null) {
            throw new InvalidOperationException("Load status is required for check-in");
        }
    }
    
    /**
     * Validates gate can be used for check-in at the specified site.
     */
    private void validateGateForCheckIn(Gate gate, Site site) {
        if (!gate.getSite().getId().equals(site.getId())) {
            throw new InvalidOperationException(
                "Gate " + gate.getName() + " (ID: " + gate.getId() + ") does not belong to site " + 
                site.getName() + " (ID: " + site.getId() + ")"
            );
        }
        
        if (gate.getFunction() != Gate.GateFunction.CHECK_IN && 
            gate.getFunction() != Gate.GateFunction.CHECK_IN_OUT) {
            throw new InvalidOperationException(
                "Gate " + gate.getName() + " (ID: " + gate.getId() + 
                ") does not support check-in operations"
            );
        }
    }
    
    /**
     * Validates carrier is eligible for the specified site.
     */
    private void validateCarrierEligibility(Carrier carrier, Site site) {
        boolean isEligible = carrier.getEligibleSites().stream()
                .anyMatch(eligibleSite -> eligibleSite.getId().equals(site.getId()));
                
        if (!isEligible) {
            throw new InvalidOperationException(
                "Carrier " + carrier.getName() + " (ID: " + carrier.getId() + 
                ") is not eligible for site " + site.getName() + " (ID: " + site.getId() + ")"
            );
        }
    }
    
    /**
     * Validates check-out request data.
     */
    private void validateCheckOutRequest(CheckOutRequestDto request) {
        if (request.getTrailerId() == null) {
            throw new InvalidOperationException("Trailer ID is required for check-out");
        }
        
        if (request.getGateId() == null) {
            throw new InvalidOperationException("Gate ID is required for check-out");
        }
        
        if (request.getSiteId() == null) {
            throw new InvalidOperationException("Site ID is required for check-out");
        }
        
        if (request.getLoadStatus() == null) {
            throw new InvalidOperationException("Load status is required for check-out");
        }
        
        if (request.getTrailerCondition() == null) {
            throw new InvalidOperationException("Trailer condition is required for check-out");
        }
    }
    
    /**
     * Validates gate can be used for check-out and site matches appointment site.
     */
    private void validateGateForCheckOut(Gate gate, Site site, Appointment appointment) {
        // Verify site matches the appointment's site
        if (!appointment.getSite().getId().equals(site.getId())) {
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
        if (gate.getFunction() != Gate.GateFunction.CHECK_OUT && 
            gate.getFunction() != Gate.GateFunction.CHECK_IN_OUT) {
            throw new InvalidOperationException(
                "Gate " + gate.getName() + " (ID: " + gate.getId() + 
                ") does not support check-out operations"
            );
        }
    }
    
    /**
     * Validates scheduled appointment data.
     */
    private void validateScheduledAppointment(AppointmentDto dto) {
        if (dto.getSiteId() == null) {
            throw new InvalidOperationException("Site ID is required for scheduling an appointment");
        }
        
        if (dto.getScheduledTime() == null) {
            throw new InvalidOperationException("Scheduled time is required");
        }
        
        if (dto.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Scheduled time cannot be in the past");
        }
        
        if (dto.getAppointmentType() == null) {
            throw new InvalidOperationException("Appointment type is required");
        }
        
        try {
            Appointment.AppointmentType.valueOf(dto.getAppointmentType());
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid appointment type: " + dto.getAppointmentType());
        }
    }
    
    // -------------------------------------------------------------------------
    // Private helper methods for entity operations
    // -------------------------------------------------------------------------
    
    /**
     * Finds or creates a trailer based on check-in information.
     */
    private Trailer findOrCreateTrailer(CheckInRequestDto checkInRequest) {
        Optional<Trailer> existingTrailer = trailerRepository.findByTrailerNumber(checkInRequest.getTrailerNumber());
        
        if (existingTrailer.isPresent()) {
            log.debug("Found existing trailer: {}", checkInRequest.getTrailerNumber());
            return existingTrailer.get();
        } else {
            log.debug("Creating new trailer: {}", checkInRequest.getTrailerNumber());
            Trailer trailer = new Trailer();
            trailer.setTrailerNumber(checkInRequest.getTrailerNumber());
            return trailer;
        }
    }
    
    /**
     * Updates a trailer with check-in information.
     */
    private void updateTrailerForCheckIn(Trailer trailer, Carrier carrier, CheckInRequestDto checkInRequest) {
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
        
        // Initialize detention tracking if applicable
        if (carrier.isDetentionEnabled()) {
            trailer.setDetentionActive(false);
        }
        
        trailerRepository.save(trailer);
    }
    
    /**
     * Creates a new appointment for check-in.
     */
    private Appointment createCheckInAppointment(
            Trailer trailer, Site site, Gate gate, CheckInRequestDto checkInRequest) {
        
        Appointment appointment = new Appointment();
        appointment.setSite(site);
        appointment.setCheckInGate(gate);
        appointment.setTrailer(trailer);
        appointment.setType(checkInRequest.getAppointmentType());
        appointment.setScheduledTime(checkInRequest.getScheduledTime());
        appointment.setActualArrivalTime(LocalDateTime.now());
        appointment.setDriverInfo(checkInRequest.getDriverInfo());
        appointment.setGuardComments(checkInRequest.getGuardComments());
        appointment.setStatus(Appointment.AppointmentStatus.CHECKED_IN);
        
        return appointment;
    }
    
    /**
     * Updates a trailer for check-out.
     */
    private void updateTrailerForCheckOut(Trailer trailer, CheckOutRequestDto checkOutRequest) {
        trailer.setCondition(checkOutRequest.getTrailerCondition());
        trailer.setLoadStatus(checkOutRequest.getLoadStatus());
        trailer.setCheckOutTime(LocalDateTime.now());
        
        // Update process status based on load status
        if (checkOutRequest.getLoadStatus() == Trailer.LoadStatus.EMPTY) {
            trailer.setProcessStatus(Trailer.ProcessStatus.UNLOADED);
        } else if (checkOutRequest.getLoadStatus() == Trailer.LoadStatus.FULL) {
            trailer.setProcessStatus(Trailer.ProcessStatus.LOADED);
        }
    }
    
    /**
     * Releases resources associated with a trailer (door, yard location, detention).
     */
    private void releaseTrailerResources(Trailer trailer) {
        // Release door assignment
        if (trailer.getAssignedDoor() != null) {
            Door door = trailer.getAssignedDoor();
            door.setCurrentTrailer(null);
            door.setStatus(Door.DoorStatus.AVAILABLE);
            trailer.setAssignedDoor(null);
        }
        
        // Release yard location
        if (trailer.getYardLocation() != null) {
            YardLocation location = trailer.getYardLocation();
            location.setCurrentTrailer(null);
            location.setStatus(YardLocation.LocationStatus.AVAILABLE);
            trailer.setYardLocation(null);
        }
        
        // Stop detention tracking
        if (trailer.isDetentionActive()) {
            trailer.setDetentionActive(false);
        }
    }
    
    /**
     * Updates appointment status and details for check-out.
     */
    private void updateAppointmentForCheckOut(
            Appointment appointment, Gate gate, CheckOutRequestDto checkOutRequest) {
        
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setCompletionTime(LocalDateTime.now());
        appointment.setCheckOutGate(gate);
        
        // Add check-out comments if provided
        if (checkOutRequest.getGuardComments() != null && !checkOutRequest.getGuardComments().isEmpty()) {
            if (appointment.getGuardComments() != null && !appointment.getGuardComments().isEmpty()) {
                appointment.setGuardComments(appointment.getGuardComments() + 
                        "\nCheck-Out Comments: " + checkOutRequest.getGuardComments());
            } else {
                appointment.setGuardComments("Check-Out Comments: " + checkOutRequest.getGuardComments());
            }
        }
    }
    
    /**
     * Gets the active appointment for a trailer.
     */
    private Appointment getActiveAppointmentForTrailer(Trailer trailer) {
        Appointment appointment = trailer.getCurrentAppointment();
        if (appointment == null) {
            throw new ResourceNotFoundException("No active appointment found for trailer ID: " + trailer.getId());
        }
        return appointment;
    }
    
    // -------------------------------------------------------------------------
    // Private helper methods for DTO conversion
    // -------------------------------------------------------------------------
    
    /**
     * Maps site data from an appointment to a DTO.
     */
    private void mapSiteToDto(Appointment appointment, AppointmentDto dto) {
        if (appointment.getSite() != null) {
            dto.setSiteId(appointment.getSite().getId());
            dto.setSiteName(appointment.getSite().getName());
        }
    }
    
    /**
     * Maps trailer data from an appointment to a DTO.
     */
    private void mapTrailerToDto(Appointment appointment, AppointmentDto dto) {
        if (appointment.getTrailer() != null) {
            dto.setTrailerId(appointment.getTrailer().getId());
            dto.setTrailerNumber(appointment.getTrailer().getTrailerNumber());
            
            if (appointment.getTrailer().getCarrier() != null) {
                dto.setCarrierId(appointment.getTrailer().getCarrier().getId());
                dto.setCarrierName(appointment.getTrailer().getCarrier().getName());
            }
        }
    }
    
    /**
     * Maps gate data from an appointment to a DTO.
     */
    private void mapGatesToDto(Appointment appointment, AppointmentDto dto) {
        if (appointment.getCheckInGate() != null) {
            dto.setCheckInGateId(appointment.getCheckInGate().getId());
            dto.setCheckInGateName(appointment.getCheckInGate().getName());
        }
        
        if (appointment.getCheckOutGate() != null) {
            dto.setCheckOutGateId(appointment.getCheckOutGate().getId());
            dto.setCheckOutGateName(appointment.getCheckOutGate().getName());
        }
    }
    
    // -------------------------------------------------------------------------
    // Private helper methods for repository access
    // -------------------------------------------------------------------------
    
    /**
     * Retrieves a site by ID or throws an exception if not found.
     */
    private Site getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
    }
    
    /**
     * Retrieves a gate by ID or throws an exception if not found.
     */
    private Gate getGateById(Long gateId) {
        return gateRepository.findById(gateId)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found with id: " + gateId));
    }
    
    /**
     * Retrieves a carrier by ID or throws an exception if not found.
     */
    private Carrier getCarrierById(Long carrierId) {
        return carrierRepository.findById(carrierId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + carrierId));
    }
    
    /**
     * Retrieves a trailer by ID or throws an exception if not found.
     */
    private Trailer getTrailerById(Long trailerId) {
        return trailerRepository.findById(trailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + trailerId));
    }
    
    /**
     * Checks if repository supports optimized active appointments query method.
     */
    private boolean supportsActiveAppointmentsQuery() {
        try {
            appointmentRepository.getClass().getMethod("findActiveBySite", Site.class);
            return true;
        } catch (NoSuchMethodException e) {
            log.warn("Repository does not implement findActiveBySite method. Using fallback filtering.");
            return false;
        }
    }
}