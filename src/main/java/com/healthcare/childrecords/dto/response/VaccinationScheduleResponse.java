package com.healthcare.childrecords.dto.response;

public record VaccinationScheduleResponse(
        Long id,
        Long vaccineId,
        String vaccineName,
        String vaccineNameGr,
        Integer recommendedAgeMonths,
        Integer doseNumber,
        String notes
) {}
