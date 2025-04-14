package com.yardflowpro.service;

import com.yardflowpro.dto.UserDto;
import com.yardflowpro.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for managing users in the yard management system.
 * <p>
 * Provides methods for creating, retrieving, updating, and deleting users,
 * as well as user-specific operations like password management and role-based queries.
 * </p>
 */
public interface UserService {
    
    /**
     * Creates a new user in the system.
     *
     * @param userDto user data transfer object containing user information
     * @param password raw password that will be encoded before storage
     * @return the created user as a DTO
     * @throws com.yardflowpro.exception.InvalidOperationException if username or email already exists
     */
    UserDto createUser(UserDto userDto, String password);
    
    /**
     * Updates an existing user's information.
     *
     * @param id the ID of the user to update
     * @param userDto user data transfer object containing updated information
     * @return the updated user as a DTO
     * @throws com.yardflowpro.exception.ResourceNotFoundException if user with given ID doesn't exist
     * @throws com.yardflowpro.exception.InvalidOperationException if updated username or email conflicts
     */
    UserDto updateUser(Long id, UserDto userDto);
    
    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return the user as a DTO
     * @throws com.yardflowpro.exception.ResourceNotFoundException if user with given ID doesn't exist
     */
    UserDto getUserById(Long id);
    
    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user to retrieve
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<UserDto> getUserByUsername(String username);
    
    /**
     * Retrieves all users in the system.
     *
     * @return list of all users as DTOs
     */
    List<UserDto> getAllUsers();
    
    /**
     * Retrieves users with a specific role.
     *
     * @param role the role to filter users by
     * @return list of users with the specified role as DTOs
     */
    List<UserDto> getUsersByRole(User.UserRole role);
    
    /**
     * Retrieves users who have access to a specific site.
     *
     * @param siteId the ID of the site to check access for
     * @return list of users with access to the site as DTOs
     */
    List<UserDto> getUsersWithSiteAccess(Long siteId);
    
    /**
     * Deletes a user from the system.
     *
     * @param id the ID of the user to delete
     * @throws com.yardflowpro.exception.ResourceNotFoundException if user with given ID doesn't exist
     */
    void deleteUser(Long id);
    
    /**
     * Changes a user's password.
     *
     * @param id the ID of the user
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @throws com.yardflowpro.exception.ResourceNotFoundException if user with given ID doesn't exist
     * @throws com.yardflowpro.exception.InvalidOperationException if old password is incorrect
     */
    void changePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * Updates a user's site access permissions.
     *
     * @param userId the ID of the user
     * @param siteIds set of site IDs the user should have access to
     * @throws com.yardflowpro.exception.ResourceNotFoundException if user or any site doesn't exist
     */
    void updateUserSiteAccess(Long userId, Set<Long> siteIds);
    
    /**
     * Activates or deactivates a user account.
     *
     * @param userId the ID of the user
     * @param active true to activate, false to deactivate
     * @throws com.yardflowpro.exception.ResourceNotFoundException if user with given ID doesn't exist
     */
    void setUserActiveStatus(Long userId, boolean active);
}