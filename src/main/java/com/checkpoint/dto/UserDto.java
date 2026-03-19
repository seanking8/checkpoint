package com.checkpoint.dto;

import com.checkpoint.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Represents a user for admin-facing endpoints (GET /api/admin/users)
// Never exposes passwordHash
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private Role role;
}

