package com.yardflowpro.dto;

import com.yardflowpro.model.Trailer.LoadStatus;
import com.yardflowpro.model.Trailer.TrailerCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequestDto {
    private Long siteId;
    private Long gateId;
    private Long trailerId;
    private TrailerCondition trailerCondition;
    private LoadStatus loadStatus;
    private String guardComments;
    private String driverInfo;
}