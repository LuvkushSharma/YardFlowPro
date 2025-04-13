package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfigurationDto {
    private Long siteId;
    private Integer maxTrailerCount;
    private Integer operatingHoursStart;
    private Integer operatingHoursEnd;
    private boolean detentionEnabled;
    private Integer freeTimeHours;
}
