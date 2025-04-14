package com.yardflowpro.service.impl;

import com.yardflowpro.dto.UserDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.User;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.repository.UserRepository;
import com.yardflowpro.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface for managing yard management system users.
 * <p>
 * This service handles user creation, updates, queries, and security operations.
 * </p>
 */
@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new UserServiceImpl with required dependencies.
     *
     * @param userRepository repository for user entities
     * @param siteRepository repository for site entities
     * @param passwordEncoder encoder for securely storing passwords
     */
    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            SiteRepository siteRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDto createUser(UserDto userDto, String password) {
        log.info("Creating new user with username: {}", userDto.getUsername());
        
        validateNewUserData(userDto);
        
        User user = new User();
        populateUserFromDto(user, userDto);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        
        if (userDto.getAccessibleSiteIds() != null && !userDto.getAccessibleSiteIds().isEmpty()) {
            Set<Site> accessibleSites = fetchSitesByIds(userDto.getAccessibleSiteIds());
            user.setAccessibleSites(accessibleSites);
        }
        
        User savedUser = userRepository.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getId());
        
        return convertToDto(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);
        
        User user = findUserById(id);
        validateUpdatedUserData(user, userDto);
        
        populateUserFromDto(user, userDto);
        
        if (userDto.getAccessibleSiteIds() != null) {
            Set<Site> accessibleSites = fetchSitesByIds(userDto.getAccessibleSiteIds());
            user.setAccessibleSites(accessibleSites);
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user with ID: {}", updatedUser.getId());
        
        return convertToDto(updatedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.debug("Retrieving user with ID: {}", id);
        User user = findUserById(id);
        return convertToDto(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        log.debug("Retrieving user with username: {}", username);
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Retrieving all users");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(User.UserRole role) {
        log.debug("Retrieving users with role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithSiteAccess(Long siteId) {
        log.debug("Retrieving users with access to site ID: {}", siteId);
        Site site = findSiteById(siteId);
        return userRepository.findBySiteAccess(site).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("Successfully deleted user with ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Changing password for user with ID: {}", id);
        User user = findUserById(id);
        
        validateOldPassword(user, oldPassword);
        validateNewPassword(newPassword);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Successfully changed password for user with ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUserSiteAccess(Long userId, Set<Long> siteIds) {
        log.info("Updating site access for user with ID: {}", userId);
        User user = findUserById(userId);
        
        Set<Site> sites = fetchSitesByIds(siteIds);
        user.setAccessibleSites(sites);
        
        userRepository.save(user);
        log.info("Successfully updated site access for user with ID: {}", userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserActiveStatus(Long userId, boolean active) {
        log.info("Setting active status to {} for user with ID: {}", active, userId);
        User user = findUserById(userId);
        
        user.setActive(active);
        userRepository.save(user);
        
        log.info("Successfully updated active status for user with ID: {}", userId);
    }

    // -------------------------------------------------------------------------
    // Private helper methods
    // -------------------------------------------------------------------------
    
    /**
     * Converts a User entity to its DTO representation.
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        
        if (user.getAccessibleSites() != null) {
            Set<Long> accessibleSiteIds = user.getAccessibleSites().stream()
                    .map(Site::getId)
                    .collect(Collectors.toSet());
            dto.setAccessibleSiteIds(accessibleSiteIds);
        }
        
        return dto;
    }
    
    /**
     * Populates a User entity with data from a DTO.
     */
    private void populateUserFromDto(User user, UserDto dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
    }
    
    /**
     * Validates that data for a new user doesn't conflict with existing users.
     */
    private void validateNewUserData(UserDto userDto) {
        // Validate required fields
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            throw new InvalidOperationException("Username cannot be empty");
        }
        
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new InvalidOperationException("Email cannot be empty");
        }
        
        // Check if username is already taken
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new InvalidOperationException("Username is already taken: " + userDto.getUsername());
        }
        
        // Check if email is already in use
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new InvalidOperationException("Email is already in use: " + userDto.getEmail());
        }
    }
    
    /**
     * Validates that updated user data doesn't conflict with existing users.
     */
    private void validateUpdatedUserData(User existingUser, UserDto updatedUserDto) {
        // Check if username is changed and already taken
        if (!existingUser.getUsername().equals(updatedUserDto.getUsername()) && 
                userRepository.findByUsername(updatedUserDto.getUsername()).isPresent()) {
            throw new InvalidOperationException("Username is already taken: " + updatedUserDto.getUsername());
        }
        
        // Check if email is changed and already in use
        if (!existingUser.getEmail().equals(updatedUserDto.getEmail()) && 
                userRepository.findByEmail(updatedUserDto.getEmail()).isPresent()) {
            throw new InvalidOperationException("Email is already in use: " + updatedUserDto.getEmail());
        }
    }
    
    /**
     * Validates that the provided old password matches the user's current password.
     */
    private void validateOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOperationException("Old password is incorrect");
        }
    }
    
    /**
     * Validates that a new password meets security requirements.
     */
    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new InvalidOperationException("Password must be at least 8 characters long");
        }
        
        // Additional password complexity rules could be added here
    }
    
    /**
     * Finds a user by ID or throws an exception if not found.
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
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
        for (Long siteId : siteIds) {
            Site site = findSiteById(siteId);
            sites.add(site);
        }
        return sites;
    }
}