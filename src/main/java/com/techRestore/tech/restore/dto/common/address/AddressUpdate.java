package com.techRestore.tech.restore.dto.common.address;

public record AddressUpdate(
        String state,
        String city,
        String street,
        String building,
        String notes,
        boolean isDefault
) {}
