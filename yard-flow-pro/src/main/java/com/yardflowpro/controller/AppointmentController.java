package com.yardflowpro.controller;

import com.yardflowpro.dto.AppointmentDto;
import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.model.Appointment;
import com.yardflowpro.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<AppointmentDto> processCheckIn(@RequestBody CheckInRequestDto checkInRequest) {
        Appointment appointment = appointmentService.processCheckIn(checkInRequest);
        return new ResponseEntity<>(appointmentService.convertToDto(appointment), HttpStatus.CREATED);
    }

    @PostMapping("/check-out")
    public ResponseEntity<AppointmentDto> processCheckOut(@RequestBody CheckOutRequestDto checkOutRequest) {
        Appointment appointment = appointmentService.processCheckOut(checkOutRequest);
        return ResponseEntity.ok(appointmentService.convertToDto(appointment));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AppointmentDto>> getActiveAppointments(@RequestParam Long siteId) {
        List<AppointmentDto> appointments = appointmentService.getActiveAppointments(siteId).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointmentService.convertToDto(appointment));
    }

    @GetMapping("/trailer/{trailerId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByTrailerId(@PathVariable Long trailerId) {
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByTrailerId(trailerId).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/gate/{gateId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByGateId(@PathVariable Long gateId) {
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByGateId(gateId).stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }
}