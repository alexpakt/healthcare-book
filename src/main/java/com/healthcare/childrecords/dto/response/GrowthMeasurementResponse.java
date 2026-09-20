package com.healthcare.childrecords.dto.response;

import java.time.LocalDate;

public record GrowthMeasurementResponse(
        Long id,
        Long childId,
        String childName,
        Long doctorId,
        String doctorName,
        LocalDate measurementDate,
        Integer ageInMonths,
        Double heightCm,
        Double weightKg,
        Double headCircumferenceCm,
        Integer percentileHeight,
        Integer percentileWeight,
        Integer percentileHeadCircumference,
        String notes
) {}
