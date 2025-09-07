package com.techRestore.tech.restore.user.dto.user;

import com.techRestore.tech.restore.common.dto.address.AddressResponse;
import com.techRestore.tech.restore.common.model.enums.Role;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserProfileDTO {
    private UUID id;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;
    private boolean activate;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AddressResponse> addresses;
}