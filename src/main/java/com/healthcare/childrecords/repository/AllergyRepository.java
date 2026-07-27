package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {
    List<Allergy> findByChildId(Long childId);
}
