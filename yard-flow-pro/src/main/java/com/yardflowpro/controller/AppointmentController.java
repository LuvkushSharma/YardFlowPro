package com.yardflowpro.controller;

import com.yardflowpro.dto.AppointmentDto;
import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.model.Appointment;
import com.yardflowpro.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing appointments in the yard management system.
 * <p>
 * Provides endpoints for processing check-ins, check-outs, and querying appointment data.
 * </p>
 */
@RestController
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Creates a new AppointmentController with the required service.
     *
     * @param appointmentService service for appointment operations
     */
    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Processes a trailer check-in operation.
     *
     * @param checkInRequest the check-in details
     * @return the created appointment as a DTO
     */
    @PostMapping("/check-in")
    public ResponseEntity<AppointmentDto> processCheckIn(@Valid @RequestBody CheckInRequestDto checkInRequest) {
        log.info("API: Processing check-in request for trailer: {}", checkInRequest.getTrailerNumber());
        Appointment appointment = appointmentService.processCheckIn(checkInRequest);
        return new ResponseEntity<>(appointmentService.convertToDto(appointment), HttpStatus.CREATED);
    }

    /**
     * Processes a trailer check-out operation.
     *
     * @param checkOutRequest the check-out details
     * @return the updated appointment as a DTO
     */
    @PostMapping("/check-out")
    public ResponseEntity<AppointmentDto> processCheckOut(@Valid @RequestBody CheckOutRequestDto checkOutRequest) {
        log.info("API: Processing check-out request for trailer ID: {}", checkOutRequest.getTrailerId());
        Appointment appointment = appointmentService.processCheckOut(checkOutRequest);
        return ResponseEntity.ok(appointmentService.convertToDto(appointment));
    }

    /**
     * Retrieves all active appointments at a specific site.
     *
     * @param siteId ID of the site to find active appointments for
     * @return list of active appointments as DTOs
     */
    @GetMapping("/active")
    public ResponseEntity<List<AppointmentDto>> getActiveAppointments(@RequestParam Long siteId) {
        log.info("API: Retrieving active appointments for site ID: {}", siteId);
        List<AppointmentDto> appointments = appointmentService.getActiveAppointments(siteId).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Retrieves an appointment by its ID.
     *
     * @param id the appointment ID
     * @return the appointment as a DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        log.info("API: Retrieving appointment with ID: {}", id);
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointmentService.convertToDto(appointment));
    }

    /**
     * Retrieves all appointments for a specific trailer.
     *
     * @param trailerId ID of the trailer to find appointments for
     * @return list of appointments as DTOs
     */
    @GetMapping("/trailer/{trailerId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByTrailerId(@PathVariable Long trailerId) {
        log.info("API: Retrieving appointments for trailer ID: {}", trailerId);
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByTrailerId(trailerId).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Retrieves all appointments that used a specific gate.
     *
     * @param gateId ID of the gate to find appointments for
     * @return list of appointments as DTOs
     */
    @GetMapping("/gate/{gateId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByGateId(@PathVariable Long gateId) {
        log.info("API: Retrieving appointments for gate ID: {}", gateId);
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByGateId(gateId).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Retrieves all appointments at a specific site.
     *
     * @param siteId ID of the site to find appointments for
     * @return list of appointments as DTOs
     */
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsBySiteId(@PathVariable Long siteId) {
        log.info("API: Retrieving all appointments for site ID: {}", siteId);
        List<AppointmentDto> appointments = appointmentService.getAppointmentsBySiteId(siteId).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Retrieves appointments scheduled within a date range.
     *
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of appointments as DTOs
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("API: Retrieving appointments in date range: {} to {}", startDate, endDate);
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Cancels an existing appointment.
     *
     * @param id the appointment ID
     * @param cancelReason reason for cancellation
     * @return the cancelled appointment as a DTO
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDto> cancelAppointment(
            @PathVariable Long id, 
            @RequestParam(required = false) String cancelReason) {
        log.info("API: Cancelling appointment with ID: {}", id);
        Appointment appointment = appointmentService.cancelAppointment(id, cancelReason);
        return ResponseEntity.ok(appointmentService.convertToDto(appointment));
    }
    
    /**
     * Updates an appointment's status to IN_PROGRESS.
     *
     * @param id the appointment ID
     * @return the updated appointment as a DTO
     */
    @PostMapping("/{id}/start-processing")
    public ResponseEntity<AppointmentDto> startProcessingAppointment(@PathVariable Long id) {
        log.info("API: Starting processing for appointment with ID: {}", id);
        Appointment appointment = appointmentService.startProcessingAppointment(id);
        return ResponseEntity.ok(appointmentService.convertToDto(appointment));
    }
    
    /**
     * Creates a scheduled appointment for a future check-in.
     *
     * @param appointmentDto appointment details
     * @return the created appointment as a DTO
     */
    @PostMapping("/schedule")
    public ResponseEntity<AppointmentDto> scheduleAppointment(@Valid @RequestBody AppointmentDto appointmentDto) {
        log.info("API: Scheduling new appointment for trailer: {}", appointmentDto.getTrailerNumber());
        Appointment appointment = appointmentService.scheduleAppointment(appointmentDto);
        return new ResponseEntity<>(appointmentService.convertToDto(appointment), HttpStatus.CREATED);
    }
}