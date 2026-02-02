package com.back.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        String newPasswordConfirm
) {
    public boolean isPasswordMatching() {
        return newPassword.equals(newPasswordConfirm);
    }
}