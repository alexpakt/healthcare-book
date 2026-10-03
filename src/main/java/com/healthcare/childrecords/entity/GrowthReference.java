package com.healthcare.childrecords.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "growth_references",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"age_in_months", "measurement_type", "gender", "percentile", "source"},
                        name = "uk_growth_reference_composite"
                )
        },
        indexes = {
                @Index(columnList = "age_in_months,measurement_type,gender", name = "idx_growth_ref_age_type_gender")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ageInMonths;

    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    @Enumerated(EnumType.STRING)
    private MeasurementType measurementType;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private Integer percentile;

    @Column(name = "measurement_value", nullable = false)
    private Double value;

    @Column(nullable = false, length = 50)
    private String source;
}
