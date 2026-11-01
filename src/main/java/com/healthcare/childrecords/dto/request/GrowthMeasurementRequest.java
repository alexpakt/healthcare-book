package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record GrowthMeasurementRequest(
        @NotNull Long childId,
        @NotNull @PastOrPresent LocalDate measurementDate,
        @Positive @Digits(integer = 3, fraction = 2) Double heightCm,
        @Positive @Digits(integer = 3, fraction = 2) Double weightKg,
        @Positive @Digits(integer = 2, fraction = 2) Double headCircumferenceCm,
        String notes
) {}
