package com.yardflowpro.controller;

import com.yardflowpro.dto.MoveRequestDto;
import com.yardflowpro.model.MoveRequest;
import com.yardflowpro.model.Site;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.service.MoveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import com.yardflowpro.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/move-requests")
public class MoveRequestController {

    private final MoveRequestService moveRequestService;
    private final SiteRepository siteRepository;

    @Autowired
    public MoveRequestController(
            MoveRequestService moveRequestService,
            SiteRepository siteRepository) { // Update constructor
        this.moveRequestService = moveRequestService;
        this.siteRepository = siteRepository;
    }

    @PostMapping
    public ResponseEntity<MoveRequest> createMoveRequest(
            @RequestBody MoveRequestDto moveRequestDto,
            @RequestParam Long requestedById) {
        return new ResponseEntity<>(moveRequestService.createMoveRequest(moveRequestDto, requestedById), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MoveRequest>> getPendingMoveRequests() {
        return ResponseEntity.ok(moveRequestService.getPendingMoveRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoveRequest> getMoveRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(moveRequestService.getMoveRequestById(id));
    }

    @GetMapping("/spotter/{spotterId}")
    public ResponseEntity<List<MoveRequest>> getMoveRequestsBySpotterId(@PathVariable Long spotterId) {
        return ResponseEntity.ok(moveRequestService.getMoveRequestsBySpotterId(spotterId));
    }

    @PatchMapping("/{id}/assign/{spotterId}")
    public ResponseEntity<MoveRequest> assignMoveRequest(
            @PathVariable Long id, 
            @PathVariable Long spotterId) {
        return ResponseEntity.ok(moveRequestService.assignMoveRequest(id, spotterId));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<MoveRequest> startMoveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(moveRequestService.startMoveRequest(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<MoveRequest> completeMoveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(moveRequestService.completeMoveRequest(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MoveRequest> cancelMoveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(moveRequestService.cancelMoveRequest(id));
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<MoveRequest>> getMoveRequestsBySite(@PathVariable Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
        return ResponseEntity.ok(moveRequestService.getMoveRequestsBySite(site));
    }

    @GetMapping("/site/{siteId}/pending")
    public ResponseEntity<List<MoveRequest>> getPendingMoveRequestsBySite(@PathVariable Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
        return ResponseEntity.ok(moveRequestService.getPendingMoveRequestsBySite(site));
    }
}