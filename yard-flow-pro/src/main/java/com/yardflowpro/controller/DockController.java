package com.yardflowpro.controller;

import com.yardflowpro.dto.DockDto;
import com.yardflowpro.model.Dock;
import com.yardflowpro.service.DockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/docks")
public class DockController {

    private final DockService dockService;

    @Autowired
    public DockController(DockService dockService) {
        this.dockService = dockService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDock(@RequestBody DockDto dockDto) {
        Dock dock = dockService.createDock(dockDto);
        return new ResponseEntity<>(dockToMap(dock), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDockById(@PathVariable Long id) {
        return ResponseEntity.ok(dockToMap(dockService.getDockById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDock(@PathVariable Long id, @RequestBody DockDto dockDto) {
        return ResponseEntity.ok(dockToMap(dockService.updateDock(id, dockDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDock(@PathVariable Long id) {
        dockService.deleteDock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllDocks() {
        return ResponseEntity.ok(dockService.getAllDocks().stream()
                .map(this::dockToMap)
                .collect(Collectors.toList()));
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<Map<String, Object>>> getDocksBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(dockService.getDocksBySite(siteId).stream()
                .map(this::dockToMap)
                .collect(Collectors.toList()));
    }
    
    // Helper method to safely convert Dock entity to Map for JSON response
    private Map<String, Object> dockToMap(Dock dock) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dock.getId());
        map.put("name", dock.getName());
        map.put("code", dock.getCode());
        
        // Add site information
        if (dock.getSite() != null) {
            Map<String, Object> siteMap = new HashMap<>();
            siteMap.put("id", dock.getSite().getId());
            siteMap.put("name", dock.getSite().getName());
            siteMap.put("code", dock.getSite().getCode());
            map.put("site", siteMap);
        }
        
        // Add door summaries
        if (dock.getDoors() != null && !dock.getDoors().isEmpty()) {
            List<Map<String, Object>> doorsList = dock.getDoors().stream()
                    .map(door -> {
                        Map<String, Object> doorMap = new HashMap<>();
                        doorMap.put("id", door.getId());
                        doorMap.put("name", door.getName());
                        doorMap.put("code", door.getCode());
                        doorMap.put("status", door.getStatus().name());
                        return doorMap;
                    })
                    .collect(Collectors.toList());
            map.put("doors", doorsList);
        }
        
        return map;
    }
}