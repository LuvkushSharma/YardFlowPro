package com.yardflowpro.dto;

import com.yardflowpro.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private User.UserRole role;
    private boolean active;
    private Set<Long> accessibleSiteIds;
}