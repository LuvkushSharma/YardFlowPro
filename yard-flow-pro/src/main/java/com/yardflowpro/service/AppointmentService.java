package com.yardflowpro.service;

import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.model.Appointment;

import java.util.List;

public interface AppointmentService {
    Appointment processCheckIn(CheckInRequestDto checkInRequest);
    Appointment processCheckOut(CheckOutRequestDto checkOutRequest);
    List<Appointment> getActiveAppointments(Long siteId);
    Appointment getAppointmentById(Long id);
    List<Appointment> getAppointmentsByTrailerId(Long trailerId);
    
}