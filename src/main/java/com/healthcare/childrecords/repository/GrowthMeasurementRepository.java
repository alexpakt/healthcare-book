package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.GrowthMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthMeasurementRepository extends JpaRepository<GrowthMeasurement, Long> {
    List<GrowthMeasurement> findByChildIdOrderByMeasurementDateAsc(Long childId);
}
