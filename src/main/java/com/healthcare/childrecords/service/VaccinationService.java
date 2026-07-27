package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.VaccinationRequest;
import com.healthcare.childrecords.dto.response.VaccinationResponse;
import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.entity.Vaccination;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.VaccinationRepository;
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
    private final DoctorService doctorService;

    @Transactional
    public VaccinationResponse create(String username, VaccinationRequest request) {
        Child child = childService.findById(request.childId());
        childService.verifyAccess(username, child);

        Doctor administeredBy = null;
        if (request.administeredByDoctorId() != null) {
            administeredBy = doctorService.findById(request.administeredByDoctorId());
        }

        Vaccination vaccination = Vaccination.builder()
                .child(child)
                .administeredBy(administeredBy)
                .vaccineName(request.vaccineName())
                .administeredDate(request.administeredDate())
                .nextDoseDate(request.nextDoseDate())
                .batchNumber(request.batchNumber())
                .notes(request.notes())
                .build();
        return toResponse(vaccinationRepository.save(vaccination));
    }

    public List<VaccinationResponse> getForChild(String username, Long childId) {
        Child child = childService.findById(childId);
        childService.verifyAccess(username, child);
        return vaccinationRepository.findByChildId(childId).stream().map(this::toResponse).toList();
    }

    public VaccinationResponse getById(String username, Long id) {
        Vaccination vaccination = findById(id);
        childService.verifyAccess(username, vaccination.getChild());
        return toResponse(vaccination);
    }

    @Transactional
    public VaccinationResponse update(String username, Long id, VaccinationRequest request) {
        Vaccination vaccination = findById(id);
        childService.verifyAccess(username, vaccination.getChild());
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
                v.getVaccineName(), v.getAdministeredDate(), v.getNextDoseDate(),
                v.getBatchNumber(), doctorName, v.getNotes());
    }
}
