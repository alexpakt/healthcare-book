package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.AppointmentRequest;
import com.healthcare.childrecords.dto.response.AppointmentResponse;
import com.healthcare.childrecords.entity.*;
import com.healthcare.childrecords.exception.AccessDeniedException;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.AppointmentRepository;
import com.healthcare.childrecords.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ChildService childService;
    private final DoctorService doctorService;
    private final ParentService parentService;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentResponse create(String username, AppointmentRequest request) {
        Child child = childService.findById(request.childId());
        childService.verifyAccess(username, child);

        Doctor doctor = doctorService.findById(request.doctorId());

        Appointment appointment = Appointment.builder()
                .child(child)
                .doctor(doctor)
                .appointmentDate(request.appointmentDate())
                .reason(request.reason())
                .notes(request.notes())
                .build();
        return toResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> getForChild(String username, Long childId) {
        Child child = childService.findById(childId);
        childService.verifyAccess(username, child);
        return appointmentRepository.findByChildId(childId).stream().map(this::toResponse).toList();
    }

    public List<AppointmentResponse> getMyAppointments(String username) {
        Role role = userRepository.findByUsername(username).orElseThrow().getRole();
        if (role == Role.ROLE_DOCTOR) {
            Doctor doctor = doctorService.findByUsername(username);
            return appointmentRepository.findByDoctorId(doctor.getId()).stream().map(this::toResponse).toList();
        }
        Parent parent = parentService.findByUsername(username);
        return parent.getChildren().stream()
                .flatMap(c -> appointmentRepository.findByChildId(c.getId()).stream())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse updateStatus(String username, Long appointmentId, AppointmentStatus status) {
        Appointment appointment = findById(appointmentId);
        verifyAccess(username, appointment);
        appointment.setStatus(status);
        return toResponse(appointmentRepository.save(appointment));
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    private void verifyAccess(String username, Appointment appointment) {
        childService.verifyAccess(username, appointment.getChild());
    }

    private AppointmentResponse toResponse(Appointment a) {
        String childName = a.getChild().getFirstName() + " " + a.getChild().getLastName();
        String doctorName = a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName();
        return new AppointmentResponse(a.getId(), a.getChild().getId(), childName,
                a.getDoctor().getId(), doctorName, a.getAppointmentDate(),
                a.getReason(), a.getNotes(), a.getStatus());
    }
}
