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
    private final ChildAccessGuard childAccessGuard;
    private final DoctorService doctorService;
    private final ParentService parentService;
    private final UserRepository userRepository;

    @Transactional
    public AppointmentResponse create(String username, AppointmentRequest request) {
        Child child = childService.findById(request.childId());
        childAccessGuard.verifyAccess(username, child);

        Doctor doctor = doctorService.findById(request.doctorId());

        Role role = userRepository.findByUsername(username).orElseThrow().getRole();
        AppointmentStatus status;
        if (role == Role.ROLE_DOCTOR) {
            Doctor callingDoctor = doctorService.findByUsername(username);
            if (!callingDoctor.getId().equals(doctor.getId())) {
                throw new AccessDeniedException("Doctors can only create appointments for themselves");
            }
            status = AppointmentStatus.SCHEDULED;
        } else {
            status = AppointmentStatus.REQUESTED;
        }

        Appointment appointment = Appointment.builder()
                .child(child)
                .doctor(doctor)
                .appointmentDate(request.appointmentDate())
                .reason(request.reason())
                .notes(request.notes())
                .status(status)
                .build();
        return toResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> getForChild(String username, Long childId) {
        Child child = childService.findById(childId);
        childAccessGuard.verifyAccess(username, child);
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
    public AppointmentResponse updateStatus(String username, Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = findById(appointmentId);
        AppointmentStatus currentStatus = appointment.getStatus();
        Role role = userRepository.findByUsername(username).orElseThrow().getRole();

        if (role == Role.ROLE_DOCTOR) {
            Doctor doctor = doctorService.findByUsername(username);
            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new AccessDeniedException("Doctor is not assigned to this appointment");
            }
            boolean valid = (currentStatus == AppointmentStatus.REQUESTED
                        && (newStatus == AppointmentStatus.SCHEDULED || newStatus == AppointmentStatus.DECLINED))
                    || (currentStatus == AppointmentStatus.SCHEDULED
                        && (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED));
            if (!valid) {
                throw new AccessDeniedException("Invalid status transition: " + currentStatus + " -> " + newStatus);
            }
            if (currentStatus == AppointmentStatus.REQUESTED && newStatus == AppointmentStatus.SCHEDULED) {
                childService.ensureDoctorAssigned(appointment.getChild(), doctor);
            }
        } else {
            Parent parent = parentService.findByUsername(username);
            if (!appointment.getChild().getParent().getId().equals(parent.getId())) {
                throw new AccessDeniedException("Access denied to this appointment");
            }
            boolean valid = (currentStatus == AppointmentStatus.REQUESTED || currentStatus == AppointmentStatus.SCHEDULED)
                    && newStatus == AppointmentStatus.CANCELLED;
            if (!valid) {
                throw new AccessDeniedException("Invalid status transition: " + currentStatus + " -> " + newStatus);
            }
        }

        appointment.setStatus(newStatus);
        return toResponse(appointmentRepository.save(appointment));
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    private AppointmentResponse toResponse(Appointment a) {
        String childName = a.getChild().getFirstName() + " " + a.getChild().getLastName();
        String doctorName = a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName();
        Parent parent = a.getChild().getParent();
        String parentName = parent.getFirstName() + " " + parent.getLastName();
        return new AppointmentResponse(a.getId(), a.getChild().getId(), childName,
                a.getDoctor().getId(), doctorName, parentName, a.getAppointmentDate(),
                a.getReason(), a.getNotes(), a.getStatus());
    }
}
