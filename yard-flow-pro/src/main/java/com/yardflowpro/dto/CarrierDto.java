package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarrierDto {
    private Long id;
    private String name;
    private String code;
    private boolean ownsTractors;
    private boolean ownsTrailers;
    private boolean detentionEnabled;
    private Integer detentionStartsAt;
    private Integer detentionStopsAt;
    private Integer freeTimeHours;
    private Integer chargeIntervalHours;
    private BigDecimal chargePerInterval;
    private boolean maxChargeEnabled;
    private BigDecimal maxCharge;
    private Set<Long> eligibleSiteIds;
}