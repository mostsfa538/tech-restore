package com.techRestore.tech.restore.user.dto.user;

import java.util.UUID;

import com.techRestore.tech.restore.common.model.enums.Role;

public record ResponseUsersDto(
                UUID id,
                String firstName,
                String lastName,
                String email,
                String phone,
                Role role,
                boolean activate) {
}
