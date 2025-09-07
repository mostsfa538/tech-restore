package com.techRestore.tech.restore.common.dto.common;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank String email) {
}
