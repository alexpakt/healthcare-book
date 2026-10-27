package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.AllergyRequest;
import com.healthcare.childrecords.dto.response.AllergyResponse;
import com.healthcare.childrecords.entity.Allergy;
import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllergyService {

    private final AllergyRepository allergyRepository;
    private final ChildService childService;
    private final ChildAccessGuard childAccessGuard;

    @Transactional
    public AllergyResponse create(String username, AllergyRequest request) {
        Child child = childService.findById(request.childId());
        childAccessGuard.verifyAccess(username, child);

        Allergy allergy = Allergy.builder()
                .child(child)
                .allergen(request.allergen())
                .severity(request.severity())
                .reaction(request.reaction())
                .diagnosedDate(request.diagnosedDate())
                .notes(request.notes())
                .build();
        return toResponse(allergyRepository.save(allergy));
    }

    public List<AllergyResponse> getForChild(String username, Long childId) {
        Child child = childService.findById(childId);
        childAccessGuard.verifyAccess(username, child);
        return allergyRepository.findByChildId(childId).stream().map(this::toResponse).toList();
    }

    public AllergyResponse getById(String username, Long id) {
        Allergy allergy = findById(id);
        childAccessGuard.verifyAccess(username, allergy.getChild());
        return toResponse(allergy);
    }

    @Transactional
    public AllergyResponse update(String username, Long id, AllergyRequest request) {
        Allergy allergy = findById(id);
        childAccessGuard.verifyAccess(username, allergy.getChild());
        allergy.setAllergen(request.allergen());
        allergy.setSeverity(request.severity());
        allergy.setReaction(request.reaction());
        allergy.setDiagnosedDate(request.diagnosedDate());
        allergy.setNotes(request.notes());
        return toResponse(allergyRepository.save(allergy));
    }

    @Transactional
    public void delete(String username, Long id) {
        Allergy allergy = findById(id);
        childAccessGuard.verifyAccess(username, allergy.getChild());
        allergyRepository.delete(allergy);
    }

    private Allergy findById(Long id) {
        return allergyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Η αλλεργία δεν βρέθηκε: " + id));
    }

    private AllergyResponse toResponse(Allergy a) {
        String childName = a.getChild().getFirstName() + " " + a.getChild().getLastName();
        return new AllergyResponse(a.getId(), a.getChild().getId(), childName,
                a.getAllergen(), a.getSeverity(), a.getReaction(),
                a.getDiagnosedDate(), a.getNotes());
    }
}
