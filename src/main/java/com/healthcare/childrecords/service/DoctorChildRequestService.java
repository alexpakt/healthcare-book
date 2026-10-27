package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.DoctorChildConnectionRequest;
import com.healthcare.childrecords.dto.response.DoctorChildRequestResponse;
import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.entity.DoctorChildRequest;
import com.healthcare.childrecords.exception.AccessDeniedException;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.DoctorChildRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorChildRequestService {

    private final DoctorChildRequestRepository requestRepository;
    private final DoctorService doctorService;
    private final ChildService childService;

    @Transactional
    public DoctorChildRequestResponse sendConnectionRequest(String doctorUsername, DoctorChildConnectionRequest request) {
        Doctor doctor = doctorService.findByUsername(doctorUsername);
        Child child = childService.findById(request.childId());

        if (child.getDoctors().contains(doctor)) {
            throw new IllegalArgumentException("Ο ιατρός είναι ήδη συνδεδεμένος με το παιδί");
        }

        DoctorChildRequest doctorChildRequest = requestRepository
                .findByDoctorIdAndChildId(doctor.getId(), child.getId())
                .map(existing -> {
                    // A request whose connection was removed by the parent may be requested again.
                    // APPROVED is included for rows revoked before the REVOKED status existed.
                    if (existing.getStatus() != DoctorChildRequest.RequestStatus.REVOKED
                            && existing.getStatus() != DoctorChildRequest.RequestStatus.APPROVED) {
                        throw new IllegalArgumentException("Υπάρχει ήδη αίτημα σύνδεσης για αυτό το παιδί");
                    }
                    existing.setStatus(DoctorChildRequest.RequestStatus.PENDING);
                    existing.setRequestedAt(LocalDateTime.now());
                    existing.setResolvedAt(null);
                    existing.setNotes(request.notes());
                    return existing;
                })
                .orElseGet(() -> DoctorChildRequest.builder()
                        .doctor(doctor)
                        .child(child)
                        .status(DoctorChildRequest.RequestStatus.PENDING)
                        .requestedAt(LocalDateTime.now())
                        .notes(request.notes())
                        .build());

        DoctorChildRequest saved = requestRepository.save(doctorChildRequest);
        return toResponse(saved);
    }

    public List<DoctorChildRequestResponse> getPendingRequestsForParent(String parentUsername) {
        // Get parent's ID from username
        // This would need ParentService to get parent by username
        // For now, we'll rely on the controller to handle this
        throw new UnsupportedOperationException("Use repository method with parentId");
    }

    public List<DoctorChildRequestResponse> getPendingRequestsForParent(Long parentId) {
        return requestRepository.findByParentIdAndStatus(parentId, DoctorChildRequest.RequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoctorChildRequestResponse approveRequest(String parentUsername, Long requestId) {
        DoctorChildRequest doctorChildRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Το αίτημα δεν βρέθηκε"));

        // Verify that the parent owns the child
        Child child = doctorChildRequest.getChild();
        if (!child.getParent().getUser().getUsername().equals(parentUsername)) {
            throw new AccessDeniedException("Δεν έχετε πρόσβαση σε αυτό το αίτημα");
        }

        // Add doctor to child's doctors list
        Doctor doctor = doctorChildRequest.getDoctor();
        if (!child.getDoctors().contains(doctor)) {
            child.getDoctors().add(doctor);
        }

        // Update request status
        doctorChildRequest.setStatus(DoctorChildRequest.RequestStatus.APPROVED);
        doctorChildRequest.setResolvedAt(LocalDateTime.now());

        DoctorChildRequest saved = requestRepository.save(doctorChildRequest);
        return toResponse(saved);
    }

    @Transactional
    public DoctorChildRequestResponse rejectRequest(String parentUsername, Long requestId, String reason) {
        DoctorChildRequest doctorChildRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Το αίτημα δεν βρέθηκε"));

        // Verify that the parent owns the child
        Child child = doctorChildRequest.getChild();
        if (!child.getParent().getUser().getUsername().equals(parentUsername)) {
            throw new AccessDeniedException("Δεν έχετε πρόσβαση σε αυτό το αίτημα");
        }

        doctorChildRequest.setStatus(DoctorChildRequest.RequestStatus.REJECTED);
        doctorChildRequest.setResolvedAt(LocalDateTime.now());
        doctorChildRequest.setNotes(reason);

        DoctorChildRequest saved = requestRepository.save(doctorChildRequest);
        return toResponse(saved);
    }

    public List<DoctorChildRequestResponse> getSentPendingRequests(String doctorUsername) {
        Doctor doctor = doctorService.findByUsername(doctorUsername);
        return requestRepository.findSentByDoctorIdAndStatus(doctor.getId(), DoctorChildRequest.RequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DoctorChildRequestResponse toResponse(DoctorChildRequest request) {
        Doctor doctor = request.getDoctor();
        Child child = request.getChild();

        return new DoctorChildRequestResponse(
                request.getId(),
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization(),
                child.getId(),
                child.getFirstName(),
                child.getLastName(),
                request.getStatus().name(),
                request.getRequestedAt(),
                request.getResolvedAt(),
                request.getNotes()
        );
    }
}
