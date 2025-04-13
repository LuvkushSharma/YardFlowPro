package com.yardflowpro.controller;

import com.yardflowpro.dto.CarrierDto;
import com.yardflowpro.service.CarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
public class CarrierController {

    private final CarrierService carrierService;

    @Autowired
    public CarrierController(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    @PostMapping
    public ResponseEntity<CarrierDto> createCarrier(@RequestBody CarrierDto carrierDto) {
        CarrierDto createdCarrier = carrierService.createCarrier(carrierDto);
        return new ResponseEntity<>(createdCarrier, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CarrierDto>> getAllCarriers() {
        List<CarrierDto> carriers = carrierService.getAllCarriers();
        return ResponseEntity.ok(carriers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarrierDto> getCarrierById(@PathVariable Long id) {
        CarrierDto carrier = carrierService.getCarrierById(id);
        return ResponseEntity.ok(carrier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarrierDto> updateCarrier(@PathVariable Long id, @RequestBody CarrierDto carrierDto) {
        CarrierDto updatedCarrier = carrierService.updateCarrier(id, carrierDto);
        return ResponseEntity.ok(updatedCarrier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarrier(@PathVariable Long id) {
        carrierService.deleteCarrier(id);
        return ResponseEntity.noContent().build();
    }
}