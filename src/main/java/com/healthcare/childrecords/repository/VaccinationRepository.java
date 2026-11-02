package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
    List<Vaccination> findByChildId(Long childId);

    @Query("SELECT v FROM Vaccination v WHERE v.child.id = :childId ORDER BY v.administeredDate DESC")
    List<Vaccination> findHistoryByChildId(@Param("childId") Long childId);
}
