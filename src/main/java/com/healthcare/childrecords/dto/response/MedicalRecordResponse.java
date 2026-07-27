package com.healthcare.childrecords.dto.response;

import java.time.LocalDate;

public record MedicalRecordResponse(
        Long id,
        Long childId,
        String childName,
        Long doctorId,
        String doctorName,
        Long appointmentId,
        LocalDate recordDate,
        String diagnosis,
        String notes,
        String prescription
) {}
