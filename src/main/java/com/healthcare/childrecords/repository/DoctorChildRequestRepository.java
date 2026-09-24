package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.DoctorChildRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorChildRequestRepository extends JpaRepository<DoctorChildRequest, Long> {
    List<DoctorChildRequest> findByChildIdAndStatus(Long childId, DoctorChildRequest.RequestStatus status);

    Optional<DoctorChildRequest> findByDoctorIdAndChildId(Long doctorId, Long childId);

    @Query("SELECT dcr FROM DoctorChildRequest dcr WHERE dcr.doctor.id = :doctorId AND dcr.status = :status ORDER BY dcr.requestedAt DESC")
    List<DoctorChildRequest> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") DoctorChildRequest.RequestStatus status);

    @Query("SELECT dcr FROM DoctorChildRequest dcr WHERE dcr.child.parent.id = :parentId AND dcr.status = :status ORDER BY dcr.requestedAt DESC")
    List<DoctorChildRequest> findByParentIdAndStatus(@Param("parentId") Long parentId, @Param("status") DoctorChildRequest.RequestStatus status);

    @Query("SELECT dcr FROM DoctorChildRequest dcr WHERE dcr.doctor.id = :doctorId AND dcr.status = :status ORDER BY dcr.requestedAt DESC")
    List<DoctorChildRequest> findSentByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") DoctorChildRequest.RequestStatus status);
}
