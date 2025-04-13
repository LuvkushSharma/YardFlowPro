package com.yardflowpro.controller;

import com.yardflowpro.dto.DoorDto;
import com.yardflowpro.model.Door;
import com.yardflowpro.service.DoorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doors")
public class DoorController {

    private final DoorService doorService;

    @Autowired
    public DoorController(DoorService doorService) {
        this.doorService = doorService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDoor(@RequestBody DoorDto doorDto) {
        Door door = doorService.createDoor(doorDto);
        return new ResponseEntity<>(doorToMap(door), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDoorById(@PathVariable Long id) {
        return ResponseEntity.ok(doorToMap(doorService.getDoorById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDoor(@PathVariable Long id, @RequestBody DoorDto doorDto) {
        return ResponseEntity.ok(doorToMap(doorService.updateDoor(id, doorDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoor(@PathVariable Long id) {
        doorService.deleteDoor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllDoors() {
        return ResponseEntity.ok(doorService.getAllDoors().stream()
                .map(this::doorToMap)
                .collect(Collectors.toList()));
    }

    @GetMapping("/dock/{dockId}")
    public ResponseEntity<List<Map<String, Object>>> getDoorsByDock(@PathVariable Long dockId) {
        return ResponseEntity.ok(doorService.getDoorsByDock(dockId).stream()
                .map(this::doorToMap)
                .collect(Collectors.toList()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Map<String, Object>>> getDoorsByStatus(@PathVariable Door.DoorStatus status) {
        return ResponseEntity.ok(doorService.getDoorsByStatus(status).stream()
                .map(this::doorToMap)
                .collect(Collectors.toList()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateDoorStatus(
            @PathVariable Long id,
            @RequestParam Door.DoorStatus status) {
        return ResponseEntity.ok(doorToMap(doorService.updateDoorStatus(id, status)));
    }
    
    // Helper method to safely convert Door entity to Map for JSON response
    private Map<String, Object> doorToMap(Door door) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", door.getId());
        map.put("name", door.getName());
        map.put("code", door.getCode());
        map.put("status", door.getStatus().name());
        
        // Add dock information
        if (door.getDock() != null) {
            Map<String, Object> dockMap = new HashMap<>();
            dockMap.put("id", door.getDock().getId());
            dockMap.put("name", door.getDock().getName());
            dockMap.put("code", door.getDock().getCode());
            map.put("dock", dockMap);
        }
        
        // Add trailer information if available
        if (door.getCurrentTrailer() != null) {
            Map<String, Object> trailerMap = new HashMap<>();
            trailerMap.put("id", door.getCurrentTrailer().getId());
            trailerMap.put("trailerNumber", door.getCurrentTrailer().getTrailerNumber());
            trailerMap.put("loadStatus", door.getCurrentTrailer().getLoadStatus() != null ? 
                    door.getCurrentTrailer().getLoadStatus().name() : null);
            map.put("currentTrailer", trailerMap);
        }
        
        return map;
    }
}