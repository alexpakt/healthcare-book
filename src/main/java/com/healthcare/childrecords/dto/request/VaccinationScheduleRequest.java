package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VaccinationScheduleRequest(
        @NotBlank(message = "Το όνομα εμβολίου είναι υποχρεωτικό") String vaccineName,
        String vaccineNameGr,
        @NotNull(message = "Η προτεινόμενη ηλικία σε μήνες είναι υποχρεωτική") Integer recommendedAgeMonths,
        Integer doseNumber,
        String notes
) {}
