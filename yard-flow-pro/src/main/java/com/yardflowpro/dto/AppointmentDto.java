package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    private Long id;
    private String siteName;
    private Long siteId;
    private String trailerNumber;
    private Long trailerId;
    private String carrierName;
    private Long carrierId;
    
    // Gate information
    private Long checkInGateId;
    private String checkInGateName;
    private Long checkOutGateId;
    private String checkOutGateName;
    
    private LocalDateTime scheduledTime;
    private LocalDateTime actualArrivalTime;
    private LocalDateTime completionTime;
    private String appointmentType;
    private String status;
    private String guardComments;
    private String driverInfo;
}