package com.yardflowpro.service;

import com.yardflowpro.dto.UserDto;
import com.yardflowpro.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto, String password);
    UserDto updateUser(Long id, UserDto userDto);
    UserDto getUserById(Long id);
    List<UserDto> getAllUsers();
    List<UserDto> getUsersByRole(User.UserRole role);
    void deleteUser(Long id);
    void changePassword(Long id, String oldPassword, String newPassword);
}