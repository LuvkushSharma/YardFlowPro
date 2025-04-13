package com.yardflowpro.dto;

import com.yardflowpro.model.Appointment.AppointmentType;
import com.yardflowpro.model.Trailer.LoadStatus;
import com.yardflowpro.model.Trailer.RefrigerationStatus;
import com.yardflowpro.model.Trailer.TrailerCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequestDto {
    private Long siteId;
    private Long gateId;
    private String trailerNumber;
    private Long carrierId;
    private LoadStatus loadStatus;
    private TrailerCondition trailerCondition;
    private RefrigerationStatus refrigerationStatus;
    private AppointmentType appointmentType;
    private LocalDateTime scheduledTime;
    private String driverInfo;
    private String guardComments;
}