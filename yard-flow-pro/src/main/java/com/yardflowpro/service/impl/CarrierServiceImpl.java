package com.yardflowpro.service.impl;

import com.yardflowpro.dto.CarrierDto;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Carrier;
import com.yardflowpro.model.Site;
import com.yardflowpro.repository.CarrierRepository;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.service.CarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CarrierServiceImpl implements CarrierService {

    private final CarrierRepository carrierRepository;
    private final SiteRepository siteRepository;

    @Autowired
    public CarrierServiceImpl(CarrierRepository carrierRepository, SiteRepository siteRepository) {
        this.carrierRepository = carrierRepository;
        this.siteRepository = siteRepository;
    }

    @Override
    public CarrierDto createCarrier(CarrierDto carrierDto) {
        Carrier carrier = new Carrier();
        carrier.setName(carrierDto.getName());
        carrier.setCode(carrierDto.getCode());
        carrier.setOwnsTractors(carrierDto.isOwnsTractors());
        carrier.setOwnsTrailers(carrierDto.isOwnsTrailers());
        carrier.setDetentionEnabled(carrierDto.isDetentionEnabled());
        carrier.setDetentionStartsAt(carrierDto.getDetentionStartsAt());
        carrier.setDetentionStopsAt(carrierDto.getDetentionStopsAt());
        carrier.setFreeTimeHours(carrierDto.getFreeTimeHours());
        carrier.setChargeIntervalHours(carrierDto.getChargeIntervalHours());
        carrier.setChargePerInterval(carrierDto.getChargePerInterval());
        carrier.setMaxChargeEnabled(carrierDto.isMaxChargeEnabled());
        carrier.setMaxCharge(carrierDto.getMaxCharge());
        
        // Set eligible sites
        if (carrierDto.getEligibleSiteIds() != null && !carrierDto.getEligibleSiteIds().isEmpty()) {
            Set<Site> eligibleSites = new HashSet<>();
            for (Long siteId : carrierDto.getEligibleSiteIds()) {
                Site site = siteRepository.findById(siteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
                eligibleSites.add(site);
            }
            carrier.setEligibleSites(eligibleSites);
        }
        
        Carrier savedCarrier = carrierRepository.save(carrier);
        return convertToDto(savedCarrier);
    }

    @Override
    public CarrierDto updateCarrier(Long id, CarrierDto carrierDto) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + id));
        
        carrier.setName(carrierDto.getName());
        carrier.setCode(carrierDto.getCode());
        carrier.setOwnsTractors(carrierDto.isOwnsTractors());
        carrier.setOwnsTrailers(carrierDto.isOwnsTrailers());
        carrier.setDetentionEnabled(carrierDto.isDetentionEnabled());
        carrier.setDetentionStartsAt(carrierDto.getDetentionStartsAt());
        carrier.setDetentionStopsAt(carrierDto.getDetentionStopsAt());
        carrier.setFreeTimeHours(carrierDto.getFreeTimeHours());
        carrier.setChargeIntervalHours(carrierDto.getChargeIntervalHours());
        carrier.setChargePerInterval(carrierDto.getChargePerInterval());
        carrier.setMaxChargeEnabled(carrierDto.isMaxChargeEnabled());
        carrier.setMaxCharge(carrierDto.getMaxCharge());
        
        // Update eligible sites
        if (carrierDto.getEligibleSiteIds() != null) {
            Set<Site> eligibleSites = new HashSet<>();
            for (Long siteId : carrierDto.getEligibleSiteIds()) {
                Site site = siteRepository.findById(siteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
                eligibleSites.add(site);
            }
            carrier.setEligibleSites(eligibleSites);
        }
        
        Carrier updatedCarrier = carrierRepository.save(carrier);
        return convertToDto(updatedCarrier);
    }

    @Override
    public CarrierDto getCarrierById(Long id) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + id));
        return convertToDto(carrier);
    }

    @Override
    public List<CarrierDto> getAllCarriers() {
        return carrierRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCarrier(Long id) {
        Carrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + id));
        carrierRepository.delete(carrier);
    }
    
    private CarrierDto convertToDto(Carrier carrier) {
        CarrierDto dto = new CarrierDto();
        dto.setId(carrier.getId());
        dto.setName(carrier.getName());
        dto.setCode(carrier.getCode());
        dto.setOwnsTractors(carrier.isOwnsTractors());
        dto.setOwnsTrailers(carrier.isOwnsTrailers());
        dto.setDetentionEnabled(carrier.isDetentionEnabled());
        dto.setDetentionStartsAt(carrier.getDetentionStartsAt());
        dto.setDetentionStopsAt(carrier.getDetentionStopsAt());
        dto.setFreeTimeHours(carrier.getFreeTimeHours());
        dto.setChargeIntervalHours(carrier.getChargeIntervalHours());
        dto.setChargePerInterval(carrier.getChargePerInterval());
        dto.setMaxChargeEnabled(carrier.isMaxChargeEnabled());
        dto.setMaxCharge(carrier.getMaxCharge());
        
        if (carrier.getEligibleSites() != null) {
            Set<Long> eligibleSiteIds = carrier.getEligibleSites().stream()
                    .map(Site::getId)
                    .collect(Collectors.toSet());
            dto.setEligibleSiteIds(eligibleSiteIds);
        }
        
        return dto;
    }
}