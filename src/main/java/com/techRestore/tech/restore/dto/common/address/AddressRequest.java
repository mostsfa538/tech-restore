package com.techRestore.tech.restore.dto.common.address;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String state,
        @NotBlank String city,
        @NotBlank String street,
        @NotBlank String building,
        String notes,
        boolean isDefault
) {
}
