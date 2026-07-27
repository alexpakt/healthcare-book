package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MedicalRecordRequest(
        @NotNull Long childId,
        Long appointmentId,
        @NotNull LocalDate recordDate,
        @NotBlank String diagnosis,
        String notes,
        String prescription
) {}
