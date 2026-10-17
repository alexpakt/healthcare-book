package com.healthcare.childrecords.repository;

import com.healthcare.childrecords.entity.GrowthAlertAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrowthAlertAcknowledgementRepository extends JpaRepository<GrowthAlertAcknowledgement, Long> {

    List<GrowthAlertAcknowledgement> findByChildId(Long childId);

    Optional<GrowthAlertAcknowledgement> findByChildIdAndAlertKey(Long childId, String alertKey);
}
