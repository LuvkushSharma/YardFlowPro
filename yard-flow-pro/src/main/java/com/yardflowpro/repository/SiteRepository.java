package com.yardflowpro.repository;

import com.yardflowpro.model.Site;
import com.yardflowpro.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Site} entities.
 * 
 * <p>Provides methods to search and retrieve sites based on various
 * criteria such as code, location, and user access.</p>
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    
    /**
     * Finds a site by its unique code.
     *
     * @param code the site code
     * @return an Optional containing the site if found
     */
    Optional<Site> findByCode(String code);
    
    /**
     * Finds sites by name containing the given string (case-insensitive).
     *
     * @param name the name fragment to search for
     * @return list of sites matching the name fragment
     */
    @Query("SELECT s FROM Site s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Site> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Finds sites in a specific city.
     *
     * @param city the city to find sites in
     * @return list of sites in the specified city
     */
    List<Site> findByCity(String city);
    
    /**
     * Finds sites in a specific state.
     *
     * @param state the state to find sites in
     * @return list of sites in the specified state
     */
    List<Site> findByState(String state);
    
    /**
     * Finds sites that a specific user has access to.
     *
     * @param user the user to check access for
     * @return list of sites the user has access to
     */
    @Query("SELECT s FROM Site s JOIN s.users u WHERE u = :user")
    List<Site> findByUserAccess(@Param("user") User user);
    
    /**
     * Finds sites with specific carrier eligibility.
     *
     * @param carrierId the ID of the carrier to check eligibility for
     * @return list of sites the carrier is eligible for
     */
    @Query("SELECT s FROM Site s JOIN s.carriers c WHERE c.id = :carrierId")
    List<Site> findByCarrierEligibility(@Param("carrierId") Long carrierId);
    
    /**
     * Finds sites with pagination support.
     *
     * @param pageable pagination information
     * @return page of sites
     */
    Page<Site> findAll(Pageable pageable);
    
    /**
     * Finds sites by name fragment with pagination.
     *
     * @param name the name fragment to search for
     * @param pageable pagination information
     * @return page of sites matching the name fragment
     */
    @Query("SELECT s FROM Site s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Site> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}