package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Gender;
import com.healthcare.childrecords.entity.GrowthReference;
import com.healthcare.childrecords.entity.MeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthReferenceRepository extends JpaRepository<GrowthReference, Long> {

    List<GrowthReference> findByAgeInMonthsAndMeasurementTypeAndGenderOrderByPercentileAsc(
            Integer ageInMonths,
            MeasurementType measurementType,
            Gender gender
    );

    GrowthReference findByAgeInMonthsAndMeasurementTypeAndGenderAndPercentile(
            Integer ageInMonths,
            MeasurementType measurementType,
            Gender gender,
            Integer percentile
    );

    boolean existsByMeasurementType(MeasurementType measurementType);

    List<GrowthReference> findByMeasurementTypeAndGenderOrderByAgeInMonthsAscPercentileAsc(
            MeasurementType measurementType,
            Gender gender
    );
}
