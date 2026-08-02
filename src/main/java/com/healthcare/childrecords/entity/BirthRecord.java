package com.healthcare.childrecords.entity;

import com.healthcare.childrecords.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "birth_records")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false, unique = true)
    private Child child;

    // Measurements at birth
    @Column(name = "birth_weight_grams")
    private Integer birthWeightGrams;

    @Column(name = "birth_length_cm")
    private Double birthLengthCm;

    @Column(name = "birth_head_circumference_cm")
    private Double birthHeadCircumferenceCm;

    @Column(name = "gestational_age_weeks")
    private Integer gestationalAgeWeeks;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type")
    private DeliveryType deliveryType;

    @Column(name = "apgar_score_1_min")
    private Integer apgarScore1Min;

    @Column(name = "apgar_score_5_min")
    private Integer apgarScore5Min;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "birth_hospital", columnDefinition = "TEXT")
    private String birthHospital;

    @Column(name = "nicu_admission")
    private Boolean nicuAdmission;

    @Column(name = "nicu_duration_days")
    private Integer nicuDurationDays;

    @Convert(converter = EncryptionConverter.class)
    @Column(columnDefinition = "TEXT")
    private String notes;
}
