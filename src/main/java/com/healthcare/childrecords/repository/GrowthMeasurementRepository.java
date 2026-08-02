package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.GrowthMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrowthMeasurementRepository extends JpaRepository<GrowthMeasurement, Long> {
    List<GrowthMeasurement> findByChildIdOrderByMeasurementDateAsc(Long childId);
}
