package com.healthcare.childrecords.dto.response;

import java.time.LocalDateTime;

public record DoctorChildRequestResponse(
        Long id,
        Long doctorId,
        String doctorFirstName,
        String doctorLastName,
        String doctorSpecialization,
        Long childId,
        String childFirstName,
        String childLastName,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime resolvedAt,
        String notes
) {}
