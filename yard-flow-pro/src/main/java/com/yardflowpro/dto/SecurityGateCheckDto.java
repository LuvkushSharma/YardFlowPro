package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityGateCheckDto {
    private Long siteId;
    private Long gateId;
    private Long trailerId;
    private boolean trailerSealed;  // Whether the trailer is sealed for security
    private String securityGuardName;
    private String inspectionComments;
    private boolean approvedForMovement;  // Whether the trailer is approved to move
}
