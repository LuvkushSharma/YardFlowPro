package com.yardflowpro.dto;

import com.yardflowpro.model.Gate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GateDto {
    private Long id;
    private String name;
    private String code;
    private Gate.GateFunction function;
    private Long siteId;
}