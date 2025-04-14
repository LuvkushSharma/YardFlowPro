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
 * Repository for managing {@link User} entities.
 * 
 * <p>Provides methods to search and retrieve users based on various
 * criteria such as username, email, role, and site access.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by their unique email.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Finds users with a specific role.
     *
     * @param role the role to filter by
     * @return list of users with the specified role
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * Finds active users.
     *
     * @return list of active users
     */
    List<User> findByActiveTrue();
    
    /**
     * Finds users with a specific role that are active.
     *
     * @param role the role to filter by
     * @return list of active users with the specified role
     */
    List<User> findByRoleAndActiveTrue(User.UserRole role);
    
    /**
     * Finds users that have access to a specific site.
     *
     * @param site the site to check access for
     * @return list of users with access to the specified site
     */
    @Query("SELECT u FROM User u JOIN u.accessibleSites s WHERE s = :site")
    List<User> findBySiteAccess(@Param("site") Site site);
    
    /**
     * Finds users that have access to a specific site and have a specific role.
     *
     * @param site the site to check access for
     * @param role the role to filter by
     * @return list of users matching both criteria
     */
    @Query("SELECT u FROM User u JOIN u.accessibleSites s WHERE s = :site AND u.role = :role")
    List<User> findBySiteAccessAndRole(@Param("site") Site site, @Param("role") User.UserRole role);
    
    /**
     * Finds users by name (first or last) containing the given string (case-insensitive).
     *
     * @param name the name fragment to search for
     * @return list of users matching the name fragment
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Finds users with pagination support.
     *
     * @param pageable pagination information
     * @return page of users
     */
    Page<User> findAll(Pageable pageable);
    
    /**
     * Finds users by role with pagination.
     *
     * @param role the role to filter by
     * @param pageable pagination information
     * @return page of users with the specified role
     */
    Page<User> findByRole(User.UserRole role, Pageable pageable);
}