package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.response.GrowthAlertResponse;
import com.healthcare.childrecords.entity.*;
import com.healthcare.childrecords.repository.GrowthAlertAcknowledgementRepository;
import com.healthcare.childrecords.repository.GrowthAlertRuleRepository;
import com.healthcare.childrecords.repository.GrowthMeasurementRepository;
import com.healthcare.childrecords.repository.GrowthReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GrowthTrajectoryServiceTest {

    private static final List<Integer> LINES = List.of(3, 15, 50, 85, 97);

    @Mock private GrowthMeasurementRepository growthMeasurementRepository;
    @Mock private GrowthReferenceRepository growthReferenceRepository;
    @Mock private GrowthAlertRuleRepository growthAlertRuleRepository;
    @Mock private GrowthAlertAcknowledgementRepository acknowledgementRepository;
    @Mock private ChildService childService;
    @Mock private ChildAccessGuard childAccessGuard;
    @Mock private DoctorService doctorService;

    private GrowthTrajectoryService service;
    private Child child;

    @BeforeEach
    void setUp() {
        service = new GrowthTrajectoryService(
                growthMeasurementRepository, growthReferenceRepository, growthAlertRuleRepository,
                acknowledgementRepository, childService, childAccessGuard, doctorService);

        child = org.mockito.Mockito.mock(Child.class);
        when(child.getId()).thenReturn(1L);
        when(child.getGender()).thenReturn(Gender.MALE);
        when(childService.findById(1L)).thenReturn(child);

        List<GrowthReference> curve = LINES.stream()
                .map(p -> GrowthReference.builder()
                        .ageInMonths(6)
                        .measurementType(MeasurementType.HEIGHT)
                        .gender(Gender.MALE)
                        .percentile(p)
                        .value((double) p)
                        .source("WHO_HEIGHT_FOR_AGE")
                        .build())
                .toList();
        when(growthReferenceRepository.findByAgeInMonthsAndMeasurementTypeAndGenderOrderByPercentileAsc(
                anyInt(), eq(MeasurementType.HEIGHT), eq(Gender.MALE))).thenReturn(curve);
    }

    @Test
    void stableCurve_noAlerts() {
        stubRule(crossingRule(null));
        stubMeasurements(measurement(1L, 6, 50.0), measurement(2L, 7, 52.0));

        assertThat(service.getAlerts("doctor", 1L)).isEmpty();
    }

    @Test
    void twoLineShift_createsAlert() {
        stubRule(crossingRule(null));
        stubMeasurements(measurement(1L, 6, 10.0), measurement(2L, 7, 60.0));

        List<GrowthAlertResponse> alerts = service.getAlerts("doctor", 1L);

        assertThat(alerts).hasSize(1);
        GrowthAlertResponse alert = alerts.get(0);
        assertThat(alert.ruleType()).isEqualTo("PERCENTILE_CROSSING");
        assertThat(alert.measurementType()).isEqualTo("HEIGHT");
        assertThat(alert.bandShift()).isEqualTo(2);
        assertThat(alert.direction()).isEqualTo("UP");
        assertThat(alert.crossedLines()).containsExactly(15, 50);
        assertThat(alert.fromMeasurementId()).isEqualTo(1L);
        assertThat(alert.toMeasurementId()).isEqualTo(2L);
        assertThat(alert.acknowledged()).isFalse();
        assertThat(alert.reason()).contains("For clinical review");
    }

    @Test
    void oneLineShift_createsAlert() {
        stubRule(crossingRule(null));
        stubMeasurements(measurement(1L, 6, 40.0), measurement(2L, 7, 60.0));

        List<GrowthAlertResponse> alerts = service.getAlerts("doctor", 1L);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).bandShift()).isEqualTo(1);
        assertThat(alerts.get(0).direction()).isEqualTo("UP");
        assertThat(alerts.get(0).crossedLines()).containsExactly(50);
    }

    @Test
    void downwardCrossing_reportsDirectionAndCrossedLines() {
        stubRule(crossingRule(null));
        stubMeasurements(measurement(1L, 6, 60.0), measurement(2L, 7, 10.0));

        List<GrowthAlertResponse> alerts = service.getAlerts("doctor", 1L);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).direction()).isEqualTo("DOWN");
        assertThat(alerts.get(0).crossedLines()).containsExactly(15, 50);
    }

    @Test
    void sameBand_noAlert() {
        stubRule(crossingRule(null));
        stubMeasurements(measurement(1L, 6, 52.0), measurement(2L, 7, 60.0));

        assertThat(service.getAlerts("doctor", 1L)).isEmpty();
    }

    @Test
    void shiftWithinSuppressedWindow_noAlert() {
        stubRule(crossingRule(6));
        stubMeasurements(measurement(1L, 3, 10.0), measurement(2L, 5, 60.0));

        assertThat(service.getAlerts("doctor", 1L)).isEmpty();
    }

    @Test
    void disabledRule_notEvaluated() {
        when(growthAlertRuleRepository.findByRuleTypeAndEnabledTrue(GrowthAlertType.PERCENTILE_CROSSING))
                .thenReturn(List.of());
        stubMeasurements(measurement(1L, 6, 10.0), measurement(2L, 7, 60.0));

        assertThat(service.getAlerts("doctor", 1L)).isEmpty();
    }

    @Test
    void missingReferenceLines_noAlert() {
        stubRule(crossingRule(null));
        when(growthReferenceRepository.findByAgeInMonthsAndMeasurementTypeAndGenderOrderByPercentileAsc(
                anyInt(), eq(MeasurementType.HEIGHT), eq(Gender.MALE))).thenReturn(List.of());
        stubMeasurements(measurement(1L, 6, 10.0), measurement(2L, 7, 60.0));

        assertThat(service.getAlerts("doctor", 1L)).isEmpty();
    }

    private GrowthAlertRule crossingRule(Integer minAgeMonths) {
        return GrowthAlertRule.builder()
                .ruleType(GrowthAlertType.PERCENTILE_CROSSING)
                .measurementType(MeasurementType.HEIGHT)
                .minAgeMonths(minAgeMonths)
                .thresholdValue(1.0)
                .percentileLines("3,15,50,85,97")
                .enabled(true)
                .build();
    }

    private void stubRule(GrowthAlertRule rule) {
        when(growthAlertRuleRepository.findByRuleTypeAndEnabledTrue(GrowthAlertType.PERCENTILE_CROSSING))
                .thenReturn(List.of(rule));
    }

    private void stubMeasurements(GrowthMeasurement... measurements) {
        when(growthMeasurementRepository.findByChildIdOrderByMeasurementDateAsc(1L))
                .thenReturn(List.of(measurements));
    }

    private GrowthMeasurement measurement(Long id, int ageInMonths, double heightCm) {
        return GrowthMeasurement.builder()
                .id(id)
                .child(child)
                .measurementDate(LocalDate.of(2020, 1, 15).plusMonths(ageInMonths))
                .ageInMonths(ageInMonths)
                .heightCm(heightCm)
                .build();
    }
}
