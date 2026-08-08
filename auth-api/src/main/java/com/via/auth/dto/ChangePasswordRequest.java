package com.via.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,

        @NotBlank
        @Pattern(
                regexp = "^(?=\\S{8,}$)(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).*$",
                message =
                        "must be at least 8 characters and include one uppercase letter, one number, and one special character")
        String newPassword) {}
