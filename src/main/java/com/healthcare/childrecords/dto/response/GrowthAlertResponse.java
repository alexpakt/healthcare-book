package com.healthcare.childrecords.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A growth alert for clinical review. Not a diagnosis.
 * direction: "UP" or "DOWN", the direction of the move between the two measurements.
 * crossedLines: the configured percentile lines (e.g. 85) that the measurement passed.
 */
public record GrowthAlertResponse(
        String alertKey,
        String ruleType,
        String measurementType,
        Long fromMeasurementId,
        Long toMeasurementId,
        LocalDate fromDate,
        LocalDate toDate,
        Double fromValue,
        Double toValue,
        Integer bandShift,
        String direction,
        List<Integer> crossedLines,
        String reason,
        boolean acknowledged,
        String acknowledgedBy,
        LocalDateTime acknowledgedAt,
        String acknowledgementNote
) {}
