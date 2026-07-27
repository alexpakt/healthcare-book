package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VaccinationRequest(
        @NotNull Long childId,
        Long administeredByDoctorId,
        @NotBlank String vaccineName,
        @NotNull LocalDate administeredDate,
        LocalDate nextDoseDate,
        String batchNumber,
        String notes
) {}
