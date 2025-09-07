package com.techRestore.tech.restore.shop.dto.shop;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShopUpdateRequest(
                @Size(min = 2, max = 50) String name,
                @Size(max = 500) String description,
                @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format") String phone) {
}
