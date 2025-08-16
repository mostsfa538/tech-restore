package com.techRestore.tech.restore.dto.user;

import com.techRestore.tech.restore.model.enums.Role;

public record ResponseUsersDto(
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role
) {}
