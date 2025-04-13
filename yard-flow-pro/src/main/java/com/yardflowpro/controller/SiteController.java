package com.yardflowpro.controller;

import com.yardflowpro.dto.SiteDto;
import com.yardflowpro.service.SiteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.
http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    @Autowired
    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }
    

    @PostMapping
    public ResponseEntity<SiteDto> createSite(@RequestBody SiteDto siteDto) {
        return new ResponseEntity<>(siteService.createSite(siteDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SiteDto>> getAllSites() {
        return ResponseEntity.ok(siteService.getAllSites());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteDto> getSiteById(@PathVariable Long id) {
        return ResponseEntity.ok(siteService.getSiteById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteDto> updateSite(@PathVariable Long id, @RequestBody SiteDto siteDto) {
        return ResponseEntity.ok(siteService.updateSite(id, siteDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.noContent().build();
    }
}