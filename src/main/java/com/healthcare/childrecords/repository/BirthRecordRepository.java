package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.BirthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BirthRecordRepository extends JpaRepository<BirthRecord, Long> {
    Optional<BirthRecord> findByChildId(Long childId);
    boolean existsByChildId(Long childId);
}
