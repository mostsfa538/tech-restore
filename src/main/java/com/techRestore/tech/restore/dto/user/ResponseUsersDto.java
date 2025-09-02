package com.techRestore.tech.restore.dto.user;

import com.techRestore.tech.restore.model.enums.Role;

import java.util.UUID;

public record ResponseUsersDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role
) {}
