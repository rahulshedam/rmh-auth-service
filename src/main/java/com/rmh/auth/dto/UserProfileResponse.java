package com.rmh.auth.dto;

import java.util.Set;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String phoneNumber,
        String addressLine,
        String city,
        String state,
        String postalCode,
        String country,
        Set<String> roles
) {}

