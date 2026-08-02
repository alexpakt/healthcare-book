package com.healthcare.childrecords.entity;

import com.healthcare.childrecords.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "growth_measurements")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "measurement_date", nullable = false)
    private LocalDate measurementDate;

    // Age in months at time of measurement, derived from child's DOB + measurementDate
    @Column(name = "age_in_months", nullable = false)
    private Integer ageInMonths;

    // All measurements in standard units; nullable so any subset can be recorded
    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "head_circumference_cm")
    private Double headCircumferenceCm;

    @Convert(converter = EncryptionConverter.class)
    @Column(columnDefinition = "TEXT")
    private String notes;
}
