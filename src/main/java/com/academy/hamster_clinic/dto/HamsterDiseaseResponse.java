package com.academy.hamster_clinic.dto;

import java.time.LocalDate;

public record HamsterDiseaseResponse(
        Long id,
        Long hamsterId,
        String hamsterName,
        Long diseaseId,
        String diseaseName,
        LocalDate diagnosedAt
) {}
