package com.techRestore.tech.restore.common.dto.common;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String email, @NotBlank String otp, @NotBlank String newPassword,
        @NotBlank String confirmPassword) {
}
