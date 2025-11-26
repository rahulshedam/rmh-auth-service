package com.rmh.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @Size(max = 20)
        @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "phone number format is invalid")
        String phoneNumber,

        @Size(max = 150)
        String addressLine,

        @Size(max = 100)
        String city,

        @Size(max = 100)
        String state,

        @Size(max = 20)
        String postalCode,

        @Size(max = 100)
        String country
) {}

