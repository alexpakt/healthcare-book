package com.healthcare.childrecords.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WHO LMS parameters for one age, measurement type and gender. See {@code GrowthPercentiles}
 * for how they turn a measurement into a percentile.
 */
@Entity
@Table(
        name = "growth_lms",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"age_in_months", "measurement_type", "gender"},
                        name = "uk_growth_lms_age_type_gender"
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthLms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "age_in_months", nullable = false)
    private Integer ageInMonths;

    @Column(name = "measurement_type", nullable = false, columnDefinition = "VARCHAR(30)")
    @Enumerated(EnumType.STRING)
    private MeasurementType measurementType;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private Double l;

    @Column(nullable = false)
    private Double m;

    @Column(nullable = false)
    private Double s;
}
