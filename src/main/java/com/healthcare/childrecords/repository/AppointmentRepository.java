package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByChildId(Long childId);
    List<Appointment> findByDoctorId(Long doctorId);
}
