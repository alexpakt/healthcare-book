package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.GrowthMeasurementRequest;
import com.healthcare.childrecords.dto.response.GrowthMeasurementResponse;
import com.healthcare.childrecords.entity.*;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.GrowthMeasurementRepository;
import com.healthcare.childrecords.repository.GrowthReferenceRepository;
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
    private final GrowthReferenceRepository growthReferenceRepository;
    private final ChildService childService;
    private final ChildAccessGuard childAccessGuard;
    private final DoctorService doctorService;

    @Transactional
    public GrowthMeasurementResponse create(String username, GrowthMeasurementRequest request) {
        if (request.heightCm() == null && request.weightKg() == null && request.headCircumferenceCm() == null) {
            throw new IllegalArgumentException("Πρέπει να συμπληρωθεί τουλάχιστον μία μέτρηση (ύψος, βάρος ή περίμετρος κεφαλής)");
        }

        Child child = childService.findById(request.childId());
        childAccessGuard.verifyAccess(username, child);
        if (request.measurementDate().isBefore(LocalDate.parse(child.getDateOfBirth()))) {
            throw new IllegalArgumentException("Η ημερομηνία μέτρησης δεν μπορεί να είναι πριν από την ημερομηνία γέννησης του παιδιού");
        }
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
        childAccessGuard.verifyAccess(username, child);
        return growthMeasurementRepository.findByChildIdOrderByMeasurementDateAsc(childId)
                .stream().map(this::toResponse).toList();
    }

    public GrowthMeasurementResponse getById(String username, Long id) {
        GrowthMeasurement measurement = findById(id);
        childAccessGuard.verifyAccess(username, measurement.getChild());
        return toResponse(measurement);
    }

    @Transactional
    public GrowthMeasurementResponse update(String username, Long id, GrowthMeasurementRequest request) {
        if (request.heightCm() == null && request.weightKg() == null && request.headCircumferenceCm() == null) {
            throw new IllegalArgumentException("Πρέπει να συμπληρωθεί τουλάχιστον μία μέτρηση");
        }

        GrowthMeasurement measurement = findById(id);
        childAccessGuard.verifyAccess(username, measurement.getChild());

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
        childAccessGuard.verifyAccess(username, measurement.getChild());
        growthMeasurementRepository.delete(measurement);
    }

    private GrowthMeasurement findById(Long id) {
        return growthMeasurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Η μέτρηση ανάπτυξης δεν βρέθηκε: " + id));
    }

    private int calculateAgeInMonths(String dateOfBirthStr, LocalDate measurementDate) {
        LocalDate dob = LocalDate.parse(dateOfBirthStr);
        return (int) Period.between(dob, measurementDate).toTotalMonths();
    }

    private GrowthMeasurementResponse toResponse(GrowthMeasurement m) {
        String childName = m.getChild().getFirstName() + " " + m.getChild().getLastName();
        String doctorName = m.getDoctor().getFirstName() + " " + m.getDoctor().getLastName();

        Integer percentileHeight = m.getHeightCm() != null
                ? calculatePercentile(m.getChild().getGender(), m.getAgeInMonths(),
                        MeasurementType.HEIGHT, m.getHeightCm())
                : null;

        Integer percentileWeight = m.getWeightKg() != null
                ? calculatePercentile(m.getChild().getGender(), m.getAgeInMonths(),
                        MeasurementType.WEIGHT, m.getWeightKg())
                : null;

        Integer percentileHeadCirc = m.getHeadCircumferenceCm() != null
                ? calculatePercentile(m.getChild().getGender(), m.getAgeInMonths(),
                        MeasurementType.HEAD_CIRCUMFERENCE, m.getHeadCircumferenceCm())
                : null;

        return new GrowthMeasurementResponse(
                m.getId(), m.getChild().getId(), childName,
                m.getDoctor().getId(), doctorName,
                m.getMeasurementDate(), m.getAgeInMonths(),
                m.getHeightCm(), m.getWeightKg(), m.getHeadCircumferenceCm(),
                percentileHeight, percentileWeight, percentileHeadCirc,
                m.getNotes());
    }

    private Integer calculatePercentile(Gender gender, Integer ageInMonths,
                                        MeasurementType measurementType, Double measuredValue) {
        List<GrowthReference> references = growthReferenceRepository
                .findByAgeInMonthsAndMeasurementTypeAndGenderOrderByPercentileAsc(
                        ageInMonths, measurementType, gender);

        if (references.isEmpty()) {
            return null;
        }

        Integer result = null;
        for (GrowthReference ref : references) {
            if (ref.getValue() <= measuredValue) {
                result = ref.getPercentile();
            }
        }

        return result;
    }

    public List<GrowthReference> getReferenceCurves(Long childId, MeasurementType measurementType) {
        Child child = childService.findById(childId);
        return growthReferenceRepository.findByMeasurementTypeAndGenderOrderByAgeInMonthsAscPercentileAsc(
                measurementType, child.getGender());
    }
}
