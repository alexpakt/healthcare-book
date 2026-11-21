package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.Appointment;
import com.healthcare.childrecords.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByChildId(Long childId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByChildIdAndDoctorIdAndStatusIn(Long childId, Long doctorId, Collection<AppointmentStatus> statuses);
}
