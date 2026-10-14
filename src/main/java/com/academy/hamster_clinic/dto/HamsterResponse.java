package com.academy.hamster_clinic.dto;

import java.time.OffsetDateTime;

public record HamsterResponse(
        Long id,
        String name,
        Long ownerId,
        String ownerFullName,
        OffsetDateTime createdAt
) {}
