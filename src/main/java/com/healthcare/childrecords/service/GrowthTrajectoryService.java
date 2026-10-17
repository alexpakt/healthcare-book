package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.response.GrowthAlertResponse;
import com.healthcare.childrecords.entity.*;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.GrowthAlertAcknowledgementRepository;
import com.healthcare.childrecords.repository.GrowthAlertRuleRepository;
import com.healthcare.childrecords.repository.GrowthMeasurementRepository;
import com.healthcare.childrecords.repository.GrowthReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Evaluates growth alert rules against a child's measurement history.
 * Alerts are computed on read and are monitoring aids for clinical review, not diagnoses.
 *
 * Only PERCENTILE_CROSSING is evaluated. VELOCITY and MISSING_MEASUREMENT rules are
 * seeded disabled and are not evaluated until a clinician defines their thresholds.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrowthTrajectoryService {

    private final GrowthMeasurementRepository growthMeasurementRepository;
    private final GrowthReferenceRepository growthReferenceRepository;
    private final GrowthAlertRuleRepository growthAlertRuleRepository;
    private final GrowthAlertAcknowledgementRepository acknowledgementRepository;
    private final ChildService childService;
    private final ChildAccessGuard childAccessGuard;
    private final DoctorService doctorService;

    public List<GrowthAlertResponse> getAlerts(String username, Long childId) {
        Child child = childService.findById(childId);
        childAccessGuard.verifyAccess(username, child);

        Map<String, GrowthAlertAcknowledgement> acknowledgements = acknowledgementRepository.findByChildId(childId)
                .stream()
                .collect(Collectors.toMap(GrowthAlertAcknowledgement::getAlertKey, a -> a));

        return evaluate(child).stream()
                .map(alert -> withAcknowledgement(alert, acknowledgements.get(alert.alertKey())))
                .toList();
    }

    @Transactional
    public GrowthAlertResponse acknowledge(String username, Long childId, String alertKey, String note) {
        Child child = childService.findById(childId);
        childAccessGuard.verifyAccess(username, child);

        GrowthAlertResponse alert = evaluate(child).stream()
                .filter(a -> a.alertKey().equals(alertKey))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Growth alert not found: " + alertKey));

        GrowthAlertAcknowledgement acknowledgement = acknowledgementRepository
                .findByChildIdAndAlertKey(childId, alertKey)
                .orElseGet(() -> acknowledgementRepository.save(GrowthAlertAcknowledgement.builder()
                        .child(child)
                        .doctor(doctorService.findByUsername(username))
                        .alertKey(alertKey)
                        .acknowledgedAt(LocalDateTime.now())
                        .note(note)
                        .build()));

        return withAcknowledgement(alert, acknowledgement);
    }

    private List<GrowthAlertResponse> evaluate(Child child) {
        List<GrowthMeasurement> measurements =
                growthMeasurementRepository.findByChildIdOrderByMeasurementDateAsc(child.getId());

        List<GrowthAlertResponse> alerts = new ArrayList<>();
        for (GrowthAlertRule rule : growthAlertRuleRepository.findByRuleTypeAndEnabledTrue(GrowthAlertType.PERCENTILE_CROSSING)) {
            alerts.addAll(evaluateCrossing(rule, child, measurements));
        }
        return alerts;
    }

    private List<GrowthAlertResponse> evaluateCrossing(GrowthAlertRule rule, Child child,
                                                       List<GrowthMeasurement> measurements) {
        if (rule.getMeasurementType() == null || rule.getThresholdValue() == null || rule.getPercentileLines() == null) {
            return List.of();
        }

        List<Integer> lines = parseLines(rule.getPercentileLines());
        List<GrowthAlertResponse> alerts = new ArrayList<>();

        for (int i = 1; i < measurements.size(); i++) {
            GrowthMeasurement from = measurements.get(i - 1);
            GrowthMeasurement to = measurements.get(i);

            Double fromValue = valueOf(from, rule.getMeasurementType());
            Double toValue = valueOf(to, rule.getMeasurementType());
            if (fromValue == null || toValue == null || !ageInRange(rule, to.getAgeInMonths())) {
                continue;
            }

            Integer fromBand = band(child, from.getAgeInMonths(), rule.getMeasurementType(), fromValue, lines);
            Integer toBand = band(child, to.getAgeInMonths(), rule.getMeasurementType(), toValue, lines);
            if (fromBand == null || toBand == null) {
                continue;
            }

            int shift = Math.abs(toBand - fromBand);
            if (shift >= rule.getThresholdValue()) {
                alerts.add(crossingAlert(rule, from, to, fromValue, toValue, fromBand, toBand, lines));
            }
        }
        return alerts;
    }

    /**
     * Index of the highest configured line at or below the value, counted from 0.
     * Returns -1 when the value is below the lowest line and null when the reference data
     * does not contain every configured line for that age.
     */
    private Integer band(Child child, Integer ageInMonths, MeasurementType type, Double value, List<Integer> lines) {
        Map<Integer, Double> valueByPercentile = growthReferenceRepository
                .findByAgeInMonthsAndMeasurementTypeAndGenderOrderByPercentileAsc(ageInMonths, type, child.getGender())
                .stream()
                .collect(Collectors.toMap(GrowthReference::getPercentile, GrowthReference::getValue, (a, b) -> a));

        if (!valueByPercentile.keySet().containsAll(lines)) {
            return null;
        }

        long linesAtOrBelow = lines.stream()
                .filter(line -> valueByPercentile.get(line) <= value)
                .count();
        return (int) linesAtOrBelow - 1;
    }

    private GrowthAlertResponse crossingAlert(GrowthAlertRule rule, GrowthMeasurement from, GrowthMeasurement to,
                                              Double fromValue, Double toValue,
                                              int fromBand, int toBand, List<Integer> lines) {
        String type = rule.getMeasurementType().name();
        int shift = Math.abs(toBand - fromBand);
        String direction = toBand > fromBand ? "UP" : "DOWN";
        return new GrowthAlertResponse(
                alertKey(rule, from, to, fromValue, toValue),
                rule.getRuleType().name(),
                type,
                from.getId(),
                to.getId(),
                from.getMeasurementDate(),
                to.getMeasurementDate(),
                fromValue,
                toValue,
                shift,
                direction,
                crossedLines(lines, fromBand, toBand),
                String.format("%s moved %d percentile band(s) between %s and %s. For clinical review.",
                        type, shift, from.getMeasurementDate(), to.getMeasurementDate()),
                false,
                null,
                null,
                null);
    }

    private GrowthAlertResponse withAcknowledgement(GrowthAlertResponse alert, GrowthAlertAcknowledgement acknowledgement) {
        if (acknowledgement == null) {
            return alert;
        }
        Doctor doctor = acknowledgement.getDoctor();
        return new GrowthAlertResponse(
                alert.alertKey(),
                alert.ruleType(),
                alert.measurementType(),
                alert.fromMeasurementId(),
                alert.toMeasurementId(),
                alert.fromDate(),
                alert.toDate(),
                alert.fromValue(),
                alert.toValue(),
                alert.bandShift(),
                alert.direction(),
                alert.crossedLines(),
                alert.reason(),
                true,
                doctor.getFirstName() + " " + doctor.getLastName(),
                acknowledgement.getAcknowledgedAt(),
                acknowledgement.getNote());
    }

    /**
     * Identifies an alert by rule type, measurement type, the two measurements and their values.
     * Changing either measurement's value produces a new key, so an acknowledgement does not carry
     * over to different evidence.
     */
    private String alertKey(GrowthAlertRule rule, GrowthMeasurement from, GrowthMeasurement to,
                            Double fromValue, Double toValue) {
        String material = String.join("|",
                rule.getRuleType().name(),
                rule.getMeasurementType().name(),
                from.getId() + ":" + fromValue,
                to.getId() + ":" + toValue);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean ageInRange(GrowthAlertRule rule, Integer ageInMonths) {
        return (rule.getMinAgeMonths() == null || ageInMonths >= rule.getMinAgeMonths())
                && (rule.getMaxAgeMonths() == null || ageInMonths <= rule.getMaxAgeMonths());
    }

    private Double valueOf(GrowthMeasurement measurement, MeasurementType type) {
        return switch (type) {
            case HEIGHT -> measurement.getHeightCm();
            case WEIGHT -> measurement.getWeightKg();
            case HEAD_CIRCUMFERENCE -> measurement.getHeadCircumferenceCm();
        };
    }

    /**
     * Band k means the value is at or above lines[k] and below lines[k+1]. A move from band a to
     * band b passes lines[min(a,b)+1] through lines[max(a,b)].
     */
    private List<Integer> crossedLines(List<Integer> lines, int fromBand, int toBand) {
        int low = Math.min(fromBand, toBand);
        int high = Math.max(fromBand, toBand);
        return List.copyOf(lines.subList(low + 1, high + 1));
    }

    private List<Integer> parseLines(String percentileLines) {
        return Arrays.stream(percentileLines.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .sorted()
                .toList();
    }
}
