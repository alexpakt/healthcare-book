package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.VaccinationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinationScheduleRepository extends JpaRepository<VaccinationSchedule, Long> {
    @Query("SELECT vs FROM VaccinationSchedule vs JOIN vs.vaccine v WHERE v.vaccineName = :vaccineName")
    List<VaccinationSchedule> findByVaccineName(@Param("vaccineName") String vaccineName);

    List<VaccinationSchedule> findByRecommendedAgeMonthsOrderByDoseNumber(Integer ageMonths);

    List<VaccinationSchedule> findAllByOrderByRecommendedAgeMonthsAscDoseNumberAsc();
}
