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
    private Long siteId;
    private Long trailerId;
    private Long carrierId;
    private LocalDateTime scheduledTime;
    private String appointmentType;  // For example: Scheduled, Unscheduled
    private String status;           // For example: Pending, Confirmed, Completed
    private String guardComments;
    private String driverInfo;
}
