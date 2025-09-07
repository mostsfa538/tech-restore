package com.techRestore.tech.restore.common.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegistration(
                @NotBlank @Size(min = 2, max = 50) String first_name,
                @NotBlank @Size(min = 2, max = 50) String last_name,
                @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format") String phone,
                @NotBlank @Email String email,
                @NotBlank @Size(min = 8, max = 100) String password) {
}
