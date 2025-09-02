package com.techRestore.tech.restore.dto.common.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOpt(@NotBlank @Email String email) {
}

