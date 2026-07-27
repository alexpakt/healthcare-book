package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByParentId(Long parentId);

    @Query("SELECT c FROM Child c JOIN c.doctors d WHERE d.id = :doctorId")
    List<Child> findByDoctorId(@Param("doctorId") Long doctorId);
}
