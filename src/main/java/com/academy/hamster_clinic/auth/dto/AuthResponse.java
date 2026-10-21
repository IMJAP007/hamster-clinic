package com.academy.hamster_clinic.auth.dto;

import java.util.List;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String fullName,
        List<String> roles
) {}