package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarrierAssignmentDto {
    private Long id;
    private Long carrierId;
    private Long siteId;
    private Long trailerId;
    private String assignmentStatus;  // E.g., Active, Completed
    private String comments;
}
