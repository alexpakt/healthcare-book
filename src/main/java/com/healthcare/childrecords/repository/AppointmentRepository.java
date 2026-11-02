package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByChildId(Long childId);
    List<Appointment> findByDoctorId(Long doctorId);
}
