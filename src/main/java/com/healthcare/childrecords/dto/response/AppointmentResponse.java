package com.healthcare.childrecords.dto.response;

import com.healthcare.childrecords.entity.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long childId,
        String childName,
        Long doctorId,
        String doctorName,
        String parentName,
        LocalDateTime appointmentDate,
        String reason,
        String notes,
        AppointmentStatus status
) {}
