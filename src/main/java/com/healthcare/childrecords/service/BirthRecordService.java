package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.BirthRecordRequest;
import com.healthcare.childrecords.dto.response.BirthRecordResponse;
import com.healthcare.childrecords.entity.BirthRecord;
import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.BirthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BirthRecordService {

    private final BirthRecordRepository birthRecordRepository;
    private final ChildService childService;

    @Transactional
    public BirthRecordResponse create(String username, BirthRecordRequest request) {
        Child child = childService.findById(request.childId());
        childService.verifyAccess(username, child);

        if (birthRecordRepository.existsByChildId(child.getId())) {
            throw new IllegalArgumentException("Birth record already exists for this child. Use PUT to update it.");
        }

        BirthRecord record = BirthRecord.builder()
                .child(child)
                .birthWeightGrams(request.birthWeightGrams())
                .birthLengthCm(request.birthLengthCm())
                .birthHeadCircumferenceCm(request.birthHeadCircumferenceCm())
                .gestationalAgeWeeks(request.gestationalAgeWeeks())
                .deliveryType(request.deliveryType())
                .apgarScore1Min(request.apgarScore1Min())
                .apgarScore5Min(request.apgarScore5Min())
                .birthHospital(request.birthHospital())
                .nicuAdmission(request.nicuAdmission())
                .nicuDurationDays(request.nicuDurationDays())
                .notes(request.notes())
                .build();

        return toResponse(birthRecordRepository.save(record));
    }

    public BirthRecordResponse getByChildId(String username, Long childId) {
        Child child = childService.findById(childId);
        childService.verifyAccess(username, child);

        return birthRecordRepository.findByChildId(childId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Birth record not found for child: " + childId));
    }

    @Transactional
    public BirthRecordResponse update(String username, Long childId, BirthRecordRequest request) {
        Child child = childService.findById(childId);
        childService.verifyAccess(username, child);

        BirthRecord record = birthRecordRepository.findByChildId(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Birth record not found for child: " + childId));

        record.setBirthWeightGrams(request.birthWeightGrams());
        record.setBirthLengthCm(request.birthLengthCm());
        record.setBirthHeadCircumferenceCm(request.birthHeadCircumferenceCm());
        record.setGestationalAgeWeeks(request.gestationalAgeWeeks());
        record.setDeliveryType(request.deliveryType());
        record.setApgarScore1Min(request.apgarScore1Min());
        record.setApgarScore5Min(request.apgarScore5Min());
        record.setBirthHospital(request.birthHospital());
        record.setNicuAdmission(request.nicuAdmission());
        record.setNicuDurationDays(request.nicuDurationDays());
        record.setNotes(request.notes());

        return toResponse(birthRecordRepository.save(record));
    }

    private BirthRecordResponse toResponse(BirthRecord r) {
        String childName = r.getChild().getFirstName() + " " + r.getChild().getLastName();
        return new BirthRecordResponse(
                r.getId(), r.getChild().getId(), childName,
                r.getBirthWeightGrams(), r.getBirthLengthCm(), r.getBirthHeadCircumferenceCm(),
                r.getGestationalAgeWeeks(), r.getDeliveryType(),
                r.getApgarScore1Min(), r.getApgarScore5Min(),
                r.getBirthHospital(), r.getNicuAdmission(), r.getNicuDurationDays(),
                r.getNotes());
    }
}
