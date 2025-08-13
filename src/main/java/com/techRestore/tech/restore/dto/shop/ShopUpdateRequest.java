package com.techRestore.tech.restore.dto.shop;

import com.techRestore.tech.restore.dto.common.address.AddressUpdate;

public record ShopUpdateRequest(
        String name,
        String description,
        String phone,
        AddressUpdate shopAddressUpdate
) {
}
