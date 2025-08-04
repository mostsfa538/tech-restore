package com.techRestore.tech.restore.dto.auth;

import jakarta.validation.constraints.*;

public record ShopRegistrationRequest(
        @NotBlank(message = "Shop name is required")
        @Size(min = 2, max = 50)
        String name,

        @NotBlank(message = "email is required")
        @Email
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8)
        String password,

        @NotBlank(message = "Description is required")
        @Size(max = 500)
        String description,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        String phone
) {
}
