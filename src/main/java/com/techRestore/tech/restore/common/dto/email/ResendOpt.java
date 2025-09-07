package com.techRestore.tech.restore.common.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOpt(@NotBlank @Email String email) {
}
