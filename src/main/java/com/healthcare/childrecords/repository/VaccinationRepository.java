package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
    List<Vaccination> findByChildId(Long childId);
}
