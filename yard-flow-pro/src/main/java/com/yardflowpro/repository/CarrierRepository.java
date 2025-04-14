package com.yardflowpro.repository;

import com.yardflowpro.model.Carrier;
import com.yardflowpro.model.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Carrier} entities.
 * 
 * <p>Provides methods to search and retrieve carriers based on various
 * criteria such as code and site eligibility.</p>
 */
@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    
    /**
     * Finds a carrier by its unique code.
     *
     * @param code the carrier code
     * @return an Optional containing the carrier if found
     */
    Optional<Carrier> findByCode(String code);
    
    /**
     * Finds carriers by name containing the given string (case-insensitive).
     *
     * @param name the name fragment to search for
     * @return list of carriers matching the name fragment
     */
    @Query("SELECT c FROM Carrier c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Carrier> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Finds carriers eligible for a specific site.
     *
     * @param site the site to check eligibility for
     * @return list of carriers eligible for the specified site
     */
    @Query("SELECT c FROM Carrier c JOIN c.eligibleSites s WHERE s = :site")
    List<Carrier> findBySiteEligibility(@Param("site") Site site);
    
    /**
     * Finds carriers with detention enabled.
     *
     * @return list of carriers with detention enabled
     */
    List<Carrier> findByDetentionEnabledTrue();
    
    /**
     * Finds carriers with pagination support.
     *
     * @param pageable pagination information
     * @return page of carriers
     */
    Page<Carrier> findAll(Pageable pageable);
    
    /**
     * Finds carriers by name fragment with pagination.
     *
     * @param name the name fragment to search for
     * @param pageable pagination information
     * @return page of carriers matching the name fragment
     */
    @Query("SELECT c FROM Carrier c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Carrier> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}