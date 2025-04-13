package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetentionChargeDto {
    private Long carrierId;
    private Long trailerId;
    private BigDecimal chargeAmount;
    private Integer hoursOverdue;
    private BigDecimal totalCharge;  // Total charge based on the hour and rate
    private boolean maxChargeExceeded;
}
