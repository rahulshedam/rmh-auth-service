package com.rmh.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RevokeRequest(@NotBlank String username) {}

