package com.academy.hamster_clinic.dto;

import java.time.OffsetDateTime;

public record OwnerResponse(
    Long id,
    String fullName,
    String phone,
    String email,
    OffsetDateTime createdAt
) {

}
