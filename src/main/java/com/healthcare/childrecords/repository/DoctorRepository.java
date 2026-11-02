package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);

    Optional<Doctor> findByAmkaHash(String amkaHash);

    boolean existsByAmkaHash(String amkaHash);

    @Query("SELECT d FROM Doctor d WHERE d.firstName ILIKE %:name% OR d.lastName ILIKE %:name%")
    List<Doctor> searchByName(@Param("name") String name);
}
