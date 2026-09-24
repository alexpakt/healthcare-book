package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.NotNull;

public record DoctorChildConnectionRequest(
        @NotNull Long childId,
        String notes
) {}
