package com.healthcare.childrecords.dto.response;

import java.util.List;

/**
 * @param componentVaccineNames for a combination vaccine (e.g. Hexavalent), the vaccines it is recorded as;
 *                              empty for a regular vaccine
 */
public record VaccineResponse(
        Long id,
        String vaccineName,
        String vaccineNameGr,
        List<String> componentVaccineNames
) {}
