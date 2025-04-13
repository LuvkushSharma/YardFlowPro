package com.yardflowpro.service.impl;

import com.yardflowpro.dto.UserDto;
import com.yardflowpro.exception.InvalidOperationException;
import com.yardflowpro.exception.ResourceNotFoundException;
import com.yardflowpro.model.Site;
import com.yardflowpro.model.User;
import com.yardflowpro.repository.SiteRepository;
import com.yardflowpro.repository.UserRepository;
import com.yardflowpro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            SiteRepository siteRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(UserDto userDto, String password) {
        // Check if username is already taken
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new InvalidOperationException("Username is already taken");
        }
        
        // Check if email is already in use
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new InvalidOperationException("Email is already in use");
        }
        
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(userDto.getRole());
        user.setActive(true);
        
        // Set accessible sites
        if (userDto.getAccessibleSiteIds() != null && !userDto.getAccessibleSiteIds().isEmpty()) {
            Set<Site> accessibleSites = new HashSet<>();
            for (Long siteId : userDto.getAccessibleSiteIds()) {
                Site site = siteRepository.findById(siteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
                accessibleSites.add(site);
            }
            user.setAccessibleSites(accessibleSites);
        }
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if username is changed and already taken
        if (!user.getUsername().equals(userDto.getUsername()) && 
                userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new InvalidOperationException("Username is already taken");
        }
        
        // Check if email is changed and already in use
        if (!user.getEmail().equals(userDto.getEmail()) && 
                userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new InvalidOperationException("Email is already in use");
        }
        
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setActive(userDto.isActive());
        
        // Update accessible sites
        if (userDto.getAccessibleSiteIds() != null) {
            Set<Site> accessibleSites = new HashSet<>();
            for (Long siteId : userDto.getAccessibleSiteIds()) {
                Site site = siteRepository.findById(siteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
                accessibleSites.add(site);
            }
            user.setAccessibleSites(accessibleSites);
        }
        
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOperationException("Old password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
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
}