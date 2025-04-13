package com.yardflowpro.controller;

import com.yardflowpro.dto.GateDto;
import com.yardflowpro.model.Gate;
import com.yardflowpro.service.GateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gates")
public class GateController {

    private final GateService gateService;

    @Autowired
    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createGate(@RequestBody GateDto gateDto) {
        Gate gate = gateService.createGate(gateDto);
        return new ResponseEntity<>(gateToMap(gate), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getGateById(@PathVariable Long id) {
        Gate gate = gateService.getGateById(id);
        return ResponseEntity.ok(gateToMap(gate));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllGates() {
        List<Map<String, Object>> gates = gateService.getAllGates().stream()
                .map(this::gateToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gates);
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<Map<String, Object>>> getGatesBySite(@PathVariable Long siteId) {
        List<Map<String, Object>> gates = gateService.getGatesBySite(siteId).stream()
                .map(this::gateToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gates);
    }

    @GetMapping("/function/{function}")
    public ResponseEntity<List<Map<String, Object>>> getGatesByFunction(
            @PathVariable Gate.GateFunction function) {
        List<Map<String, Object>> gates = gateService.getGatesByFunction(function).stream()
                .map(this::gateToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gates);
    }

    @GetMapping("/site/{siteId}/function/{function}")
    public ResponseEntity<List<Map<String, Object>>> getGatesBySiteAndFunction(
            @PathVariable Long siteId,
            @PathVariable Gate.GateFunction function) {
        List<Map<String, Object>> gates = gateService.getGatesBySiteAndFunction(siteId, function).stream()
                .map(this::gateToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateGate(
            @PathVariable Long id,
            @RequestBody GateDto gateDto) {
        Gate gate = gateService.updateGate(id, gateDto);
        return ResponseEntity.ok(gateToMap(gate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGate(@PathVariable Long id) {
        gateService.deleteGate(id);
        return ResponseEntity.noContent().build();
    }

    // Helper method to safely convert Gate entity to Map for JSON response
    private Map<String, Object> gateToMap(Gate gate) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", gate.getId());
        map.put("name", gate.getName());
        map.put("code", gate.getCode());
        map.put("function", gate.getFunction().name());
        
        // Add site information
        if (gate.getSite() != null) {
            Map<String, Object> siteMap = new HashMap<>();
            siteMap.put("id", gate.getSite().getId());
            siteMap.put("name", gate.getSite().getName());
            siteMap.put("code", gate.getSite().getCode());
            map.put("site", siteMap);
        }
        
        return map;
    }
}