package com.yardflowpro.service;

import com.yardflowpro.dto.GateDto;
import com.yardflowpro.model.Gate;

import java.util.List;

public interface GateService {
    Gate createGate(GateDto gateDto);
    Gate getGateById(Long id);
    List<Gate> getAllGates();
    List<Gate> getGatesBySite(Long siteId);
    List<Gate> getGatesByFunction(Gate.GateFunction function);
    List<Gate> getGatesBySiteAndFunction(Long siteId, Gate.GateFunction function);
    Gate updateGate(Long id, GateDto gateDto);
    void deleteGate(Long id);
}