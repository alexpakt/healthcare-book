package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull Long childId,
        @NotNull Long doctorId,
        @NotNull @Future LocalDateTime appointmentDate,
        String reason,
        String notes
) {}
