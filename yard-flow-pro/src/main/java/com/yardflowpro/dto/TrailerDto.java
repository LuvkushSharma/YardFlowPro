package com.yardflowpro.dto;

import com.yardflowpro.model.Trailer.LoadStatus;
import com.yardflowpro.model.Trailer.TrailerCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrailerDto {
    private Long id;
    private String trailerNumber;
    private LoadStatus loadStatus;
    private TrailerCondition trailerCondition;
    private String location;  
    private boolean refrigerated;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String carrierName;
    private Long carrierId;
    private String appointmentStatus;
    private Long appointmentId;
    private boolean detentionActive;
}