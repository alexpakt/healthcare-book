package com.healthcare.childrecords.dto.response;

import com.healthcare.childrecords.entity.AllergySeverity;

import java.time.LocalDate;

public record AllergyResponse(
        Long id,
        Long childId,
        String childName,
        String allergen,
        AllergySeverity severity,
        String reaction,
        LocalDate diagnosedDate,
        String notes
) {}
