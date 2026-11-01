package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByChildId(Long childId);
}
