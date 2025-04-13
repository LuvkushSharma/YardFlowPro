package com.yardflowpro.dto;

import com.yardflowpro.model.MoveRequest.MoveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequestDto {
    private Long trailerId;
    private MoveType moveType;
    private String sourceLocationType;
    private Long sourceLocationId;
    private String destinationLocationType;
    private Long destinationLocationId;
    private String notes;
}