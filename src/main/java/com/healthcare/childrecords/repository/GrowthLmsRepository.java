package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Gender;
import com.healthcare.childrecords.entity.GrowthLms;
import com.healthcare.childrecords.entity.MeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrowthLmsRepository extends JpaRepository<GrowthLms, Long> {

    Optional<GrowthLms> findByAgeInMonthsAndMeasurementTypeAndGender(
            Integer ageInMonths,
            MeasurementType measurementType,
            Gender gender
    );
}
