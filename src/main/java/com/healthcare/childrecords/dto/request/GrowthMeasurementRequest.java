package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GrowthMeasurementRequest(
        @NotNull Long childId,
        @NotNull @PastOrPresent LocalDate measurementDate,
        @Positive Double heightCm,
        @Positive Double weightKg,
        @Positive Double headCircumferenceCm,
        String notes
) {}
