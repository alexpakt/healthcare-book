package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.MedicalRecordRequest;
import com.healthcare.childrecords.dto.response.MedicalRecordResponse;
import com.healthcare.childrecords.entity.*;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.MedicalRecordRepository;
import com.healthcare.childrecords.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final ChildService childService;
    private final ChildAccessGuard childAccessGuard;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    @Transactional
    public MedicalRecordResponse create(String username, MedicalRecordRequest request) {
        Child child = childService.findById(request.childId());
        childAccessGuard.verifyAccess(username, child);

        Doctor doctor = doctorService.findByUsername(username);

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentService.findById(request.appointmentId());
        }

        MedicalRecord record = MedicalRecord.builder()
                .child(child)
                .doctor(doctor)
                .appointment(appointment)
                .recordDate(request.recordDate())
                .diagnosis(request.diagnosis())
                .notes(request.notes())
                .prescription(request.prescription())
                .build();
        return toResponse(medicalRecordRepository.save(record));
    }

    public List<MedicalRecordResponse> getForChild(String username, Long childId) {
        Child child = childService.findById(childId);
        childAccessGuard.verifyAccess(username, child);
        return medicalRecordRepository.findByChildId(childId).stream().map(this::toResponse).toList();
    }

    public MedicalRecordResponse getById(String username, Long id) {
        MedicalRecord record = findById(id);
        childAccessGuard.verifyAccess(username, record.getChild());
        return toResponse(record);
    }

    @Transactional
    public MedicalRecordResponse update(String username, Long id, MedicalRecordRequest request) {
        MedicalRecord record = findById(id);
        childAccessGuard.verifyAccess(username, record.getChild());

        // Only the creating doctor can update
        Doctor doctor = doctorService.findByUsername(username);
        if (!record.getDoctor().getId().equals(doctor.getId())) {
            throw new com.healthcare.childrecords.exception.AccessDeniedException("Μόνο ο ιατρός που δημιούργησε την εγγραφή μπορεί να την ενημερώσει");
        }

        record.setDiagnosis(request.diagnosis());
        record.setNotes(request.notes());
        record.setPrescription(request.prescription());
        record.setRecordDate(request.recordDate());
        return toResponse(medicalRecordRepository.save(record));
    }

    private MedicalRecord findById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Το ιατρικό αρχείο δεν βρέθηκε: " + id));
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        String childName = r.getChild().getFirstName() + " " + r.getChild().getLastName();
        String doctorName = r.getDoctor().getFirstName() + " " + r.getDoctor().getLastName();
        Long appointmentId = r.getAppointment() != null ? r.getAppointment().getId() : null;
        return new MedicalRecordResponse(r.getId(), r.getChild().getId(), childName,
                r.getDoctor().getId(), doctorName, appointmentId,
                r.getRecordDate(), r.getDiagnosis(), r.getNotes(), r.getPrescription());
    }
}
