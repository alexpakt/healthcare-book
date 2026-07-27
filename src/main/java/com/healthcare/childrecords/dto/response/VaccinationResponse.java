package com.healthcare.childrecords.dto.response;

import java.time.LocalDate;

public record VaccinationResponse(
        Long id,
        Long childId,
        String childName,
        String vaccineName,
        LocalDate administeredDate,
        LocalDate nextDoseDate,
        String batchNumber,
        String administeredByDoctorName,
        String notes
) {}
