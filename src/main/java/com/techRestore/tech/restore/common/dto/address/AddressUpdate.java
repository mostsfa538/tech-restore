package com.techRestore.tech.restore.common.dto.address;

public record AddressUpdate(
        String state,
        String city,
        String street,
        String building,
        String notes,
        boolean isDefault,
        Double latitude,
        Double longitude) {
}
