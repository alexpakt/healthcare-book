package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.GrowthMeasurementRequest;
import com.healthcare.childrecords.dto.response.GrowthMeasurementResponse;
import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.entity.GrowthMeasurement;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.GrowthMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrowthMeasurementService {

    private final GrowthMeasurementRepository growthMeasurementRepository;
    private final ChildService childService;
    private final DoctorService doctorService;

    @Transactional
    public GrowthMeasurementResponse create(String username, GrowthMeasurementRequest request) {
        if (request.heightCm() == null && request.weightKg() == null && request.headCircumferenceCm() == null) {
            throw new IllegalArgumentException("At least one measurement (height, weight, or head circumference) must be provided");
        }

        Child child = childService.findById(request.childId());
        childService.verifyAccess(username, child);
        Doctor doctor = doctorService.findByUsername(username);

        GrowthMeasurement measurement = GrowthMeasurement.builder()
                .child(child)
                .doctor(doctor)
                .measurementDate(request.measurementDate())
                .ageInMonths(calculateAgeInMonths(child.getDateOfBirth(), request.measurementDate()))
                .heightCm(request.heightCm())
                .weightKg(request.weightKg())
                .headCircumferenceCm(request.headCircumferenceCm())
                .notes(request.notes())
                .build();

        return toResponse(growthMeasurementRepository.save(measurement));
    }

    public List<GrowthMeasurementResponse> getForChild(String username, Long childId) {
        Child child = childService.findById(childId);
        childService.verifyAccess(username, child);
        return growthMeasurementRepository.findByChildIdOrderByMeasurementDateAsc(childId)
                .stream().map(this::toResponse).toList();
    }

    public GrowthMeasurementResponse getById(String username, Long id) {
        GrowthMeasurement measurement = findById(id);
        childService.verifyAccess(username, measurement.getChild());
        return toResponse(measurement);
    }

    @Transactional
    public GrowthMeasurementResponse update(String username, Long id, GrowthMeasurementRequest request) {
        if (request.heightCm() == null && request.weightKg() == null && request.headCircumferenceCm() == null) {
            throw new IllegalArgumentException("At least one measurement must be provided");
        }

        GrowthMeasurement measurement = findById(id);
        childService.verifyAccess(username, measurement.getChild());

        measurement.setMeasurementDate(request.measurementDate());
        measurement.setAgeInMonths(calculateAgeInMonths(measurement.getChild().getDateOfBirth(), request.measurementDate()));
        measurement.setHeightCm(request.heightCm());
        measurement.setWeightKg(request.weightKg());
        measurement.setHeadCircumferenceCm(request.headCircumferenceCm());
        measurement.setNotes(request.notes());

        return toResponse(growthMeasurementRepository.save(measurement));
    }

    @Transactional
    public void delete(String username, Long id) {
        GrowthMeasurement measurement = findById(id);
        childService.verifyAccess(username, measurement.getChild());
        growthMeasurementRepository.delete(measurement);
    }

    private GrowthMeasurement findById(Long id) {
        return growthMeasurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Growth measurement not found: " + id));
    }

    private int calculateAgeInMonths(String dateOfBirthStr, LocalDate measurementDate) {
        LocalDate dob = LocalDate.parse(dateOfBirthStr);
        return (int) Period.between(dob, measurementDate).toTotalMonths();
    }

    private GrowthMeasurementResponse toResponse(GrowthMeasurement m) {
        String childName = m.getChild().getFirstName() + " " + m.getChild().getLastName();
        String doctorName = m.getDoctor().getFirstName() + " " + m.getDoctor().getLastName();
        return new GrowthMeasurementResponse(
                m.getId(), m.getChild().getId(), childName,
                m.getDoctor().getId(), doctorName,
                m.getMeasurementDate(), m.getAgeInMonths(),
                m.getHeightCm(), m.getWeightKg(), m.getHeadCircumferenceCm(),
                m.getNotes());
    }
}
