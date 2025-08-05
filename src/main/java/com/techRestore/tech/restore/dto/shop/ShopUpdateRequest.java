package com.techRestore.tech.restore.dto.shop;

public record ShopUpdateRequest(
        String name,
        String description,
        String phone
        ) {
}
