package com.techRestore.tech.restore.dto.common.address;

public record AddressResponse(
        String state,
        String city,
        String street,
        String building,
        String notes,
        boolean isDefault
) {
}
