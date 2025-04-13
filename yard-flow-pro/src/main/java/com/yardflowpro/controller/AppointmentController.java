package com.yardflowpro.controller;

import com.yardflowpro.dto.CheckInRequestDto;
import com.yardflowpro.dto.CheckOutRequestDto;
import com.yardflowpro.model.Appointment;
import com.yardflowpro.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Object>> processCheckIn(@RequestBody CheckInRequestDto checkInRequest) {
        Appointment appointment = appointmentService.processCheckIn(checkInRequest);
        return new ResponseEntity<>(appointmentToMap(appointment), HttpStatus.CREATED);
    }

    @PostMapping("/check-out")
    public ResponseEntity<Map<String, Object>> processCheckOut(@RequestBody CheckOutRequestDto checkOutRequest) {
        Appointment appointment = appointmentService.processCheckOut(checkOutRequest);
        return ResponseEntity.ok(appointmentToMap(appointment));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveAppointments(@RequestParam Long siteId) {
        List<Map<String, Object>> appointments = appointmentService.getActiveAppointments(siteId).stream()
                .map(this::appointmentToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointmentToMap(appointment));
    }

    @GetMapping("/trailer/{trailerId}")
    public ResponseEntity<List<Map<String, Object>>> getAppointmentsByTrailerId(@PathVariable Long trailerId) {
        List<Map<String, Object>> appointments = appointmentService.getAppointmentsByTrailerId(trailerId).stream()
                .map(this::appointmentToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointments);
    }
    
    // Helper method to safely convert Appointment entity to Map for JSON response
    private Map<String, Object> appointmentToMap(Appointment appointment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", appointment.getId());
        map.put("type", appointment.getType() != null ? appointment.getType().name() : null);
        map.put("status", appointment.getStatus() != null ? appointment.getStatus().name() : null);
        map.put("scheduledTime", appointment.getScheduledTime());
        map.put("actualArrivalTime", appointment.getActualArrivalTime());
        map.put("driverInfo", appointment.getDriverInfo());
        map.put("guardComments", appointment.getGuardComments());
        
        // Add site information
        if (appointment.getSite() != null) {
            Map<String, Object> siteMap = new HashMap<>();
            siteMap.put("id", appointment.getSite().getId());
            siteMap.put("name", appointment.getSite().getName());
            siteMap.put("code", appointment.getSite().getCode());
            map.put("site", siteMap);
        }
        
        // Add trailer information
        if (appointment.getTrailer() != null) {
            Map<String, Object> trailerMap = new HashMap<>();
            trailerMap.put("id", appointment.getTrailer().getId());
            trailerMap.put("trailerNumber", appointment.getTrailer().getTrailerNumber());
            trailerMap.put("loadStatus", appointment.getTrailer().getLoadStatus() != null ? 
                    appointment.getTrailer().getLoadStatus().name() : null);
            trailerMap.put("processStatus", appointment.getTrailer().getProcessStatus() != null ? 
                    appointment.getTrailer().getProcessStatus().name() : null);
            map.put("trailer", trailerMap);
        }
        
        return map;
    }
}