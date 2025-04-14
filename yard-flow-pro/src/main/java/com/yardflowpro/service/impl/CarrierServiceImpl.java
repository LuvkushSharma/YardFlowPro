package com.yardflowpro.service.impl;

import com.yardflowpro.dto.CarrierDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Carrier;
import com.yardflowpro.model.Site;
import com.yardflowpro.repository.CarrierRepository;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.service.CarrierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the CarrierService interface for managing carrier entities.
 * <p>
 * This service handles the business logic for carrier operations including
 * CRUD operations, detention configuration, and site eligibility management.
 * </p>
 */
@Service
@Slf4j
public class CarrierServiceImpl implements CarrierService {

    private final CarrierRepository carrierRepository;
    private final SiteRepository siteRepository;

    /**
     * Creates a new CarrierServiceImpl with required repositories.
     *
     * @param carrierRepository repository for carrier entities
     * @param siteRepository repository for site entities
     */
    @Autowired
    public CarrierServiceImpl(CarrierRepository carrierRepository, SiteRepository siteRepository) {
        this.carrierRepository = carrierRepository;
        this.siteRepository = siteRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CarrierDto createCarrier(CarrierDto carrierDto) {
        log.info("Creating new carrier with code: {}", carrierDto.getCode());
        
        // Validate carrier code uniqueness
        validateCarrierCode(carrierDto.getCode(), null);
        
        // Create new carrier
        Carrier carrier = new Carrier();
        populateCarrierFromDto(carrier, carrierDto);
        
        // Set eligible sites if provided
        if (carrierDto.getEligibleSiteIds() != null && !carrierDto.getEligibleSiteIds().isEmpty()) {
            carrier.setEligibleSites(fetchSitesByIds(carrierDto.getEligibleSiteIds()));
        }
        
        // Save carrier
        Carrier savedCarrier = carrierRepository.save(carrier);
        log.info("Successfully created carrier with ID: {}", savedCarrier.getId());
        
        return convertToDto(savedCarrier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CarrierDto updateCarrier(Long id, CarrierDto carrierDto) {
        log.info("Updating carrier with ID: {}", id);
        
        // Find existing carrier
        Carrier carrier = findCarrierById(id);
        
        // Validate carrier code uniqueness if changed
        if (!carrier.getCode().equals(carrierDto.getCode())) {
            validateCarrierCode(carrierDto.getCode(), id);
        }
        
        // Update carrier properties
        populateCarrierFromDto(carrier, carrierDto);
        
        // Update eligible sites if provided
        if (carrierDto.getEligibleSiteIds() != null) {
            carrier.setEligibleSites(fetchSitesByIds(carrierDto.getEligibleSiteIds()));
        }
        
        // Save updated carrier
        Carrier updatedCarrier = carrierRepository.save(carrier);
        log.info("Successfully updated carrier with ID: {}", updatedCarrier.getId());
        
        return convertToDto(updatedCarrier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CarrierDto getCarrierById(Long id) {
        log.debug("Retrieving carrier with ID: {}", id);
        Carrier carrier = findCarrierById(id);
        return convertToDto(carrier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CarrierDto> getCarrierByCode(String code) {
        log.debug("Retrieving carrier with code: {}", code);
        return carrierRepository.findByCode(code)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CarrierDto> getAllCarriers() {
        log.debug("Retrieving all carriers");
        return carrierRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarrierDto> getAllCarriers(Pageable pageable) {
        log.debug("Retrieving carriers page: {}", pageable);
        return carrierRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CarrierDto> searchCarriersByName(String name) {
        log.debug("Searching carriers by name: {}", name);
        return carrierRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarrierDto> searchCarriersByName(String name, Pageable pageable) {
        log.debug("Searching carriers by name: {} with pagination: {}", name, pageable);
        return carrierRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CarrierDto> getCarriersBySiteEligibility(Long siteId) {
        log.debug("Retrieving carriers eligible for site ID: {}", siteId);
        Site site = findSiteById(siteId);
        
        return carrierRepository.findBySiteEligibility(site).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CarrierDto> getCarriersWithDetention() {
        log.debug("Retrieving carriers with detention enabled");
        return carrierRepository.findByDetentionEnabledTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CarrierDto updateCarrierSiteEligibility(Long carrierId, Set<Long> siteIds) {
        log.info("Updating site eligibility for carrier ID: {}", carrierId);
        
        Carrier carrier = findCarrierById(carrierId);
        Set<Site> sites = fetchSitesByIds(siteIds);
        
        carrier.setEligibleSites(sites);
        
        Carrier updatedCarrier = carrierRepository.save(carrier);
        log.info("Successfully updated site eligibility for carrier ID: {}", carrierId);
        
        return convertToDto(updatedCarrier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteCarrier(Long id) {
        log.info("Deleting carrier with ID: {}", id);
        
        Carrier carrier = findCarrierById(id);
        
        // Check if carrier has any associated trailers
        if (carrier.getTrailers() != null && !carrier.getTrailers().isEmpty()) {
            throw new InvalidOperationException(
                    "Cannot delete carrier with ID: " + id + " as it has " + 
                    carrier.getTrailers().size() + " associated trailers. " +
                    "Reassign the trailers first.");
        }
        
        carrierRepository.delete(carrier);
        log.info("Successfully deleted carrier with ID: {}", id);
    }

    // -------------------------------------------------------------------------
    // Private helper methods
    // -------------------------------------------------------------------------
    
    /**
     * Populates a Carrier entity with data from a DTO.
     */
    private void populateCarrierFromDto(Carrier carrier, CarrierDto dto) {
        carrier.setName(dto.getName());
        carrier.setCode(dto.getCode());
        carrier.setOwnsTractors(dto.isOwnsTractors());
        carrier.setOwnsTrailers(dto.isOwnsTrailers());
        
        // Detention settings
        carrier.setDetentionEnabled(dto.isDetentionEnabled());
        carrier.setDetentionStartsAt(dto.getDetentionStartsAt());
        carrier.setDetentionStopsAt(dto.getDetentionStopsAt());
        carrier.setFreeTimeHours(dto.getFreeTimeHours());
        carrier.setChargeIntervalHours(dto.getChargeIntervalHours());
        carrier.setChargePerInterval(dto.getChargePerInterval());
        carrier.setMaxChargeEnabled(dto.isMaxChargeEnabled());
        carrier.setMaxCharge(dto.getMaxCharge());
        
        // Additional validation for detention settings
        validateDetentionSettings(carrier);
    }
    
    /**
     * Validates detention settings for logical consistency.
     */
    private void validateDetentionSettings(Carrier carrier) {
        if (carrier.isDetentionEnabled()) {
            // Ensure required fields for detention are populated
            if (carrier.getFreeTimeHours() == null || carrier.getFreeTimeHours() < 0) {
                log.warn("Setting default free time hours to 24 for carrier: {}", carrier.getName());
                carrier.setFreeTimeHours(24);
            }
            
            if (carrier.getChargeIntervalHours() == null || carrier.getChargeIntervalHours() <= 0) {
                log.warn("Setting default charge interval to 1 hour for carrier: {}", carrier.getName());
                carrier.setChargeIntervalHours(1);
            }
            
            if (carrier.getChargePerInterval() == null) {
                log.warn("Detention is enabled but charge per interval is not set for carrier: {}", carrier.getName());
            }
        }
    }
    
    /**
     * Validates that a carrier code is unique.
     */
    private void validateCarrierCode(String code, Long excludeId) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidOperationException("Carrier code cannot be empty");
        }
        
        carrierRepository.findByCode(code).ifPresent(existingCarrier -> {
            // If we're updating a carrier, allow the same code if it's the same carrier
            if (excludeId == null || !existingCarrier.getId().equals(excludeId)) {
                throw new InvalidOperationException("Carrier code already exists: " + code);
            }
        });
    }
    
    /**
     * Converts a Carrier entity to its DTO representation.
     */
    private CarrierDto convertToDto(Carrier carrier) {
        CarrierDto dto = new CarrierDto();
        dto.setId(carrier.getId());
        dto.setName(carrier.getName());
        dto.setCode(carrier.getCode());
        dto.setOwnsTractors(carrier.isOwnsTractors());
        dto.setOwnsTrailers(carrier.isOwnsTrailers());
        
        // Detention settings
        dto.setDetentionEnabled(carrier.isDetentionEnabled());
        dto.setDetentionStartsAt(carrier.getDetentionStartsAt());
        dto.setDetentionStopsAt(carrier.getDetentionStopsAt());
        dto.setFreeTimeHours(carrier.getFreeTimeHours());
        dto.setChargeIntervalHours(carrier.getChargeIntervalHours());
        dto.setChargePerInterval(carrier.getChargePerInterval());
        dto.setMaxChargeEnabled(carrier.isMaxChargeEnabled());
        dto.setMaxCharge(carrier.getMaxCharge());
        
        // Eligible sites
        if (carrier.getEligibleSites() != null) {
            Set<Long> eligibleSiteIds = carrier.getEligibleSites().stream()
                    .map(Site::getId)
                    .collect(Collectors.toSet());
            dto.setEligibleSiteIds(eligibleSiteIds);
        }
        
        return dto;
    }
    
    /**
     * Finds a carrier by ID or throws an exception if not found.
     */
    private Carrier findCarrierById(Long id) {
        return carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + id));
    }
    
    /**
     * Finds a site by ID or throws an exception if not found.
     */
    private Site findSiteById(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));
    }
    
    /**
     * Fetches multiple sites by their IDs.
     */
    private Set<Site> fetchSitesByIds(Set<Long> siteIds) {
        Set<Site> sites = new HashSet<>();
        
        if (siteIds != null) {
            for (Long siteId : siteIds) {
                Site site = findSiteById(siteId);
                sites.add(site);
            }
        }
        
        return sites;
    }
}