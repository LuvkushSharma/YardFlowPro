package com.yardflowpro.dto;

import com.yardflowpro.model.Trailer.LoadStatus;
import com.yardflowpro.model.Trailer.TrailerCondition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequestDto {
    @NotNull(message = "Site ID is required")
    private Long siteId;
    
    @NotNull(message = "Gate ID is required")
    private Long gateId;
    
    @NotNull(message = "Trailer ID is required")
    private Long trailerId;
    
    private TrailerCondition trailerCondition;
    private LoadStatus loadStatus;
    private String guardComments;
    private String driverInfo;
}