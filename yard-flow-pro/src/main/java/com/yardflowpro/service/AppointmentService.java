package com.yardflowpro.service;

import com.yardflowpro.dto.AppointmentDto;
import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing trailer appointments in the yard management system.
 * <p>
 * Provides methods for processing trailer check-ins and check-outs, retrieving
 * appointment information, and managing appointment lifecycle.
 * </p>
 */
public interface AppointmentService {
    
    /**
     * Processes a trailer check-in operation.
     * <p>
     * Creates or updates a trailer record and creates a new appointment.
     * Validates business rules such as carrier eligibility and gate function.
     * </p>
     *
     * @param checkInRequest the check-in details
     * @return the created appointment
     * @throws com.yardflowpro.exception.ResourceNotFoundException if site, gate, or carrier is not found
     * @throws com.yardflowpro.exception.InvalidOperationException if business rules are violated
     */
    Appointment processCheckIn(CheckInRequestDto checkInRequest);
    
    /**
     * Processes a trailer check-out operation.
     * <p>
     * Updates the trailer status, releases resources (doors, yard locations),
     * and completes the appointment. Validates business rules such as site matching
     * and gate function.
     * </p>
     *
     * @param checkOutRequest the check-out details
     * @return the updated appointment
     * @throws com.yardflowpro.exception.ResourceNotFoundException if trailer, gate, or site is not found
     * @throws com.yardflowpro.exception.InvalidOperationException if business rules are violated
     */
    Appointment processCheckOut(CheckOutRequestDto checkOutRequest);
    
    /**
     * Retrieves all active appointments (CHECKED_IN or IN_PROGRESS) at a specific site.
     *
     * @param siteId ID of the site to find active appointments for
     * @return list of active appointments at the specified site
     * @throws com.yardflowpro.exception.ResourceNotFoundException if site is not found
     */
    List<Appointment> getActiveAppointments(Long siteId);
    
    /**
     * Retrieves an appointment by its ID.
     *
     * @param id the appointment ID
     * @return the appointment with the specified ID
     * @throws com.yardflowpro.exception.ResourceNotFoundException if appointment is not found
     */
    Appointment getAppointmentById(Long id);
    
    /**
     * Retrieves all appointments for a specific trailer.
     *
     * @param trailerId ID of the trailer to find appointments for
     * @return list of appointments for the specified trailer
     * @throws com.yardflowpro.exception.ResourceNotFoundException if trailer is not found
     */
    List<Appointment> getAppointmentsByTrailerId(Long trailerId);
    
    /**
     * Retrieves all appointments that used a specific gate for check-in or check-out.
     *
     * @param gateId ID of the gate to find appointments for
     * @return list of appointments that used the specified gate
     * @throws com.yardflowpro.exception.ResourceNotFoundException if gate is not found
     */
    List<Appointment> getAppointmentsByGateId(Long gateId);
    
    /**
     * Retrieves all appointments at a specific site.
     *
     * @param siteId ID of the site to find appointments for
     * @return list of all appointments at the specified site
     * @throws com.yardflowpro.exception.ResourceNotFoundException if site is not found
     */
    List<Appointment> getAppointmentsBySiteId(Long siteId);
    
    /**
     * Retrieves appointments scheduled within a date range.
     *
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of appointments scheduled within the specified range
     */
    List<Appointment> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Cancels an existing appointment.
     *
     * @param id the appointment ID
     * @param cancelReason reason for cancellation
     * @return the cancelled appointment
     * @throws com.yardflowpro.exception.ResourceNotFoundException if appointment is not found
     * @throws com.yardflowpro.exception.InvalidOperationException if appointment is already completed or cancelled
     */
    Appointment cancelAppointment(Long id, String cancelReason);
    
    /**
     * Updates an appointment's status to IN_PROGRESS.
     *
     * @param id the appointment ID
     * @return the updated appointment
     * @throws com.yardflowpro.exception.ResourceNotFoundException if appointment is not found
     * @throws com.yardflowpro.exception.InvalidOperationException if status transition is invalid
     */
    Appointment startProcessingAppointment(Long id);
    
    /**
     * Creates a scheduled appointment for a future check-in.
     *
     * @param appointmentDto appointment details
     * @return the created appointment
     * @throws com.yardflowpro.exception.ResourceNotFoundException if site or carrier is not found
     * @throws com.yardflowpro.exception.InvalidOperationException if validation fails
     */
    Appointment scheduleAppointment(AppointmentDto appointmentDto);
    
    /**
     * Converts an Appointment entity to its DTO representation.
     *
     * @param appointment the appointment entity to convert
     * @return the appointment DTO
     */
    AppointmentDto convertToDto(Appointment appointment);
    
    /**
     * Converts an AppointmentDto to its entity representation.
     *
     * @param appointmentDto the appointment DTO to convert
     * @return the appointment entity
     */
    Appointment convertToEntity(AppointmentDto appointmentDto);
}