package com.rmh.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RevokeRequest(
        @NotBlank
        @Email
        String email) {}

