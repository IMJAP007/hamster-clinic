package com.academy.hamster_clinic.model.dto;

import java.time.OffsetDateTime;

public record HamsterResponse(
        Long id,
        String name,
        Long ownerId,
        String ownerFullName,
        String imageUrl,
        OffsetDateTime createdAt
) {}
