package com.yardflowpro.service;

import com.yardflowpro.dto.CarrierDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for managing carrier entities in the yard management system.
 * <p>
 * Provides methods to create, retrieve, update, and delete carriers,
 * as well as methods for querying carriers based on various criteria.
 * </p>
 */
public interface CarrierService {
    
    /**
     * Creates a new carrier in the system.
     *
     * @param carrierDto carrier data transfer object containing carrier information
     * @return the created carrier as a DTO
     * @throws com.yardflowpro.exception.InvalidOperationException if carrier code already exists
     */
    CarrierDto createCarrier(CarrierDto carrierDto);
    
    /**
     * Updates an existing carrier's information.
     *
     * @param id the ID of the carrier to update
     * @param carrierDto carrier data transfer object containing updated information
     * @return the updated carrier as a DTO
     * @throws com.yardflowpro.exception.ResourceNotFoundException if carrier with given ID doesn't exist
     * @throws com.yardflowpro.exception.InvalidOperationException if updated code conflicts with existing carrier
     */
    CarrierDto updateCarrier(Long id, CarrierDto carrierDto);
    
    /**
     * Retrieves a carrier by its ID.
     *
     * @param id the ID of the carrier to retrieve
     * @return the carrier as a DTO
     * @throws com.yardflowpro.exception.ResourceNotFoundException if carrier with given ID doesn't exist
     */
    CarrierDto getCarrierById(Long id);
    
    /**
     * Retrieves a carrier by its unique code.
     *
     * @param code the code of the carrier to retrieve
     * @return an Optional containing the carrier if found, empty otherwise
     */
    Optional<CarrierDto> getCarrierByCode(String code);
    
    /**
     * Retrieves all carriers in the system.
     *
     * @return list of all carriers as DTOs
     */
    List<CarrierDto> getAllCarriers();
    
    /**
     * Retrieves carriers with pagination support.
     *
     * @param pageable pagination information
     * @return page of carriers
     */
    Page<CarrierDto> getAllCarriers(Pageable pageable);
    
    /**
     * Searches for carriers by name (case-insensitive, partial match).
     *
     * @param name the name fragment to search for
     * @return list of carriers matching the search criteria
     */
    List<CarrierDto> searchCarriersByName(String name);
    
    /**
     * Searches for carriers by name with pagination support.
     *
     * @param name the name fragment to search for
     * @param pageable pagination information
     * @return page of carriers matching the search criteria
     */
    Page<CarrierDto> searchCarriersByName(String name, Pageable pageable);
    
    /**
     * Retrieves carriers eligible for a specific site.
     *
     * @param siteId the ID of the site to check eligibility for
     * @return list of carriers eligible for the specified site
     * @throws com.yardflowpro.exception.ResourceNotFoundException if site with given ID doesn't exist
     */
    List<CarrierDto> getCarriersBySiteEligibility(Long siteId);
    
    /**
     * Retrieves carriers that have detention fees enabled.
     *
     * @return list of carriers with detention fees enabled
     */
    List<CarrierDto> getCarriersWithDetention();
    
    /**
     * Updates a carrier's site eligibility (which sites they can deliver to/pick up from).
     *
     * @param carrierId the ID of the carrier
     * @param siteIds set of site IDs the carrier should be eligible for
     * @return the updated carrier as a DTO
     * @throws com.yardflowpro.exception.ResourceNotFoundException if carrier or any site doesn't exist
     */
    CarrierDto updateCarrierSiteEligibility(Long carrierId, Set<Long> siteIds);
    
    /**
     * Deletes a carrier from the system.
     *
     * @param id the ID of the carrier to delete
     * @throws com.yardflowpro.exception.ResourceNotFoundException if carrier with given ID doesn't exist
     * @throws com.yardflowpro.exception.InvalidOperationException if carrier has associated trailers
     */
    void deleteCarrier(Long id);
}