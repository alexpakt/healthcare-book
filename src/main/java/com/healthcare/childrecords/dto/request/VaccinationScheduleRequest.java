package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VaccinationScheduleRequest(
        @NotBlank(message = "Vaccine name is required") String vaccineName,
        String vaccineNameGr,
        @NotNull(message = "Recommended age in months is required") Integer recommendedAgeMonths,
        Integer doseNumber,
        String notes
) {}
