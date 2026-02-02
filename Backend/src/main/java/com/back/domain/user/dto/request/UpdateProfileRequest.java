package com.back.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Nickname is required")
        @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters")
        String nickname,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 50, message = "Email must not exceed 50 characters")
        String email
) {
}
