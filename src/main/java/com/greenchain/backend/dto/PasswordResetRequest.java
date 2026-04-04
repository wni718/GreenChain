package com.greenchain.backend.dto;

public record PasswordResetRequest(
        String email,
        String newPassword,
        String confirmPassword
) {}
