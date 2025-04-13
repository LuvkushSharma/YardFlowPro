package com.yardflowpro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsDto {
    private Long userId;
    private Set<String> allowedActions;  // E.g., View, Edit, Delete, Assign
    private Set<Long> accessibleSiteIds;
    private Set<Long> accessibleGateIds;
}
