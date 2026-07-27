package com.healthcare.childrecords.dto.request;

import com.healthcare.childrecords.entity.AllergySeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AllergyRequest(
        @NotNull Long childId,
        @NotBlank String allergen,
        @NotNull AllergySeverity severity,
        String reaction,
        LocalDate diagnosedDate,
        String notes
) {}
