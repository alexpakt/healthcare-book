package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Vaccine;
import com.healthcare.childrecords.entity.VaccineCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineCombinationRepository extends JpaRepository<VaccineCombination, Long> {
    @Query("SELECT vc.componentVaccine FROM VaccineCombination vc WHERE vc.combinationVaccine.id = :combinationVaccineId")
    List<Vaccine> findComponentsByVaccineId(@Param("combinationVaccineId") Long combinationVaccineId);

}