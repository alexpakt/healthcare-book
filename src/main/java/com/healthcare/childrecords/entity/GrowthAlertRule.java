package com.healthcare.childrecords.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "growth_alert_rules")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthAlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private GrowthAlertType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_type", columnDefinition = "VARCHAR(30)")
    private MeasurementType measurementType;

    @Column(name = "min_age_months")
    private Integer minAgeMonths;

    @Column(name = "max_age_months")
    private Integer maxAgeMonths;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "percentile_lines", length = 100)
    private String percentileLines;

    @Column(nullable = false)
    private boolean enabled;
}
