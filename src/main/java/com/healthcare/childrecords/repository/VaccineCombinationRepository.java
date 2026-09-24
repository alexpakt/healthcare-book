package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.VaccineCombination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineCombinationRepository extends JpaRepository<VaccineCombination, Long> {
    @Query("SELECT vc.componentVaccine FROM VaccineCombination vc WHERE vc.combinationVaccine.id = :combinationVaccineId")
    List<com.healthcare.childrecords.entity.Vaccine> findComponentsByVaccineId(@Param("combinationVaccineId") Long combinationVaccineId);

    @Query("SELECT vc.componentVaccine FROM VaccineCombination vc WHERE vc.combinationVaccine.vaccineName = :vaccineName")
    List<com.healthcare.childrecords.entity.Vaccine> findComponentsByVaccineName(@Param("vaccineName") String vaccineName);

    @Query("SELECT vc FROM VaccineCombination vc WHERE vc.combinationVaccine.id = :combinationVaccineId")
    List<VaccineCombination> findByCombinationVaccineId(@Param("combinationVaccineId") Long combinationVaccineId);
}