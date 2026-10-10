package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.VaccinationRequest;
import com.healthcare.childrecords.dto.response.VaccinationResponse;
import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.entity.Vaccination;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.VaccinationRepository;
import com.healthcare.childrecords.repository.VaccineRepository;
import com.healthcare.childrecords.repository.VaccineCombinationRepository;
import com.healthcare.childrecords.repository.VaccinationScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final ChildService childService;
    private final ChildAccessGuard childAccessGuard;
    private final DoctorService doctorService;
    private final VaccineRepository vaccineRepository;
    private final VaccineCombinationRepository vaccineCombinationRepository;
    private final VaccinationScheduleRepository vaccinationScheduleRepository;

    @Transactional
    public VaccinationResponse create(String username, VaccinationRequest request) {
        Child child = childService.findById(request.childId());
        childAccessGuard.verifyAccess(username, child);

        Doctor administeredBy = null;
        if (request.administeredByDoctorId() != null) {
            administeredBy = doctorService.findById(request.administeredByDoctorId());
        }

        // Check if this is a combination vaccine
        var vaccine = vaccineRepository.findByVaccineName(request.vaccineName());
        var components = vaccine.isPresent()
            ? vaccineCombinationRepository.findComponentsByVaccineId(vaccine.get().getId())
            : java.util.List.of();

        // If it's a combination vaccine, only record components
        if (!components.isEmpty()) {
            return recordCombinationVaccineComponentsOnly(child, administeredBy, request);
        }

        // For regular vaccines, create a single record
        Integer doseNumber = getNextMissingDoseNumber(request.vaccineName(), child.getId());

        Vaccination vaccination = Vaccination.builder()
                .child(child)
                .administeredBy(administeredBy)
                .vaccineName(request.vaccineName())
                .doseNumber(doseNumber)
                .administeredDate(request.administeredDate())
                .nextDoseDate(request.nextDoseDate())
                .batchNumber(request.batchNumber())
                .notes(request.notes())
                .build();

        Vaccination savedVaccination = vaccinationRepository.save(vaccination);
        return toResponse(savedVaccination);
    }

    private Integer getNextMissingDoseNumber(String vaccineName, Long childId) {
        // Get all doses for this vaccine from the schedule
        var scheduledDoses = vaccinationScheduleRepository.findByVaccineName(vaccineName);
        if (scheduledDoses.isEmpty()) {
            return 1;
        }

        // Get all recorded doses for this vaccine for this child
        var recordedVaccinations = vaccinationRepository.findByChildId(childId).stream()
                .filter(v -> v.getVaccineName().equals(vaccineName))
                .toList();

        // Find the first missing dose
        for (var schedule : scheduledDoses.stream()
                .sorted((a, b) -> Integer.compare(
                        a.getDoseNumber() != null ? a.getDoseNumber() : 0,
                        b.getDoseNumber() != null ? b.getDoseNumber() : 0))
                .toList()) {

            boolean doseRecorded = recordedVaccinations.stream()
                    .anyMatch(v -> v.getDoseNumber() != null &&
                            v.getDoseNumber().equals(schedule.getDoseNumber()));

            if (!doseRecorded) {
                return schedule.getDoseNumber() != null ? schedule.getDoseNumber() : 1;
            }
        }

        // All scheduled doses recorded, use next dose number
        int maxDose = scheduledDoses.stream()
                .mapToInt(v -> v.getDoseNumber() != null ? v.getDoseNumber() : 0)
                .max()
                .orElse(0);
        return maxDose + 1;
    }

    private VaccinationResponse recordCombinationVaccineComponentsOnly(Child child, Doctor administeredBy, VaccinationRequest request) {
        var vaccine = vaccineRepository.findByVaccineName(request.vaccineName());
        if (vaccine.isEmpty()) {
            return null;
        }

        var components = vaccineCombinationRepository.findComponentsByVaccineId(vaccine.get().getId());
        Vaccination lastSavedComponent = null;

        for (var component : components) {
            Integer componentDoseNumber = getNextMissingDoseNumber(component.getVaccineName(), child.getId());

            Vaccination componentVaccination = Vaccination.builder()
                    .child(child)
                    .administeredBy(administeredBy)
                    .vaccineName(component.getVaccineName())
                    .doseNumber(componentDoseNumber)
                    .administeredDate(request.administeredDate())
                    .nextDoseDate(request.nextDoseDate())
                    .batchNumber(request.batchNumber())
                    .notes(request.notes())
                    .build();
            lastSavedComponent = vaccinationRepository.save(componentVaccination);
        }

        return lastSavedComponent != null ? toResponse(lastSavedComponent) : null;
    }

    public List<VaccinationResponse> getForChild(String username, Long childId) {
        Child child = childService.findById(childId);
        childAccessGuard.verifyAccess(username, child);
        return vaccinationRepository.findByChildIdExcludingComponents(childId).stream().map(this::toResponse).toList();
    }

    public VaccinationResponse getById(String username, Long id) {
        Vaccination vaccination = findById(id);
        childAccessGuard.verifyAccess(username, vaccination.getChild());
        return toResponse(vaccination);
    }

    @Transactional
    public VaccinationResponse update(String username, Long id, VaccinationRequest request) {
        Vaccination vaccination = findById(id);
        childAccessGuard.verifyAccess(username, vaccination.getChild());
        vaccination.setVaccineName(request.vaccineName());
        vaccination.setAdministeredDate(request.administeredDate());
        vaccination.setNextDoseDate(request.nextDoseDate());
        vaccination.setBatchNumber(request.batchNumber());
        vaccination.setNotes(request.notes());
        return toResponse(vaccinationRepository.save(vaccination));
    }

    private Vaccination findById(Long id) {
        return vaccinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination not found: " + id));
    }

    private VaccinationResponse toResponse(Vaccination v) {
        String childName = v.getChild().getFirstName() + " " + v.getChild().getLastName();
        String doctorName = v.getAdministeredBy() != null
                ? v.getAdministeredBy().getFirstName() + " " + v.getAdministeredBy().getLastName()
                : null;
        return new VaccinationResponse(v.getId(), v.getChild().getId(), childName,
                v.getVaccineName(), v.getDoseNumber(), v.getAdministeredDate(), v.getNextDoseDate(),
                v.getBatchNumber(), doctorName, v.getNotes());
    }
}
