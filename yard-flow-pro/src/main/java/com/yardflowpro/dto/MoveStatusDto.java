package com.yardflowpro.dto;

import com.yardflowpro.model.MoveRequest.MoveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveStatusDto {
    private Long moveRequestId;
    private Long trailerId;
    private MoveType moveType;
    private String sourceLocation;
    private String destinationLocation;
    private String currentStatus;  // E.g., Pending, In-Progress, Completed
    private String notes;
}
