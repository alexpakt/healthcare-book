package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.UpdateDoctorProfileRequest;
import com.healthcare.childrecords.dto.response.DoctorResponse;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.exception.AccessDeniedException;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.DoctorRepository;
import com.healthcare.childrecords.repository.UserRepository;
import com.healthcare.childrecords.util.AmkaHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public DoctorResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public DoctorResponse getMyProfile(String username) {
        return toResponse(findByUsername(username));
    }

    public List<DoctorResponse> getAll() {
        return doctorRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DoctorResponse updateMyProfile(String username, UpdateDoctorProfileRequest request) {
        Doctor doctor = findByUsername(username);
        if (request.firstName() != null) doctor.setFirstName(request.firstName());
        if (request.lastName() != null) doctor.setLastName(request.lastName());
        if (request.email() != null) doctor.setEmail(request.email());
        if (request.phoneNumber() != null) doctor.setPhoneNumber(request.phoneNumber());
        if (request.specialization() != null) doctor.setSpecialization(request.specialization());
        if (request.licenseNumber() != null) doctor.setLicenseNumber(request.licenseNumber());
        if (request.address() != null) doctor.setAddress(request.address());
        return toResponse(doctorRepository.save(doctor));
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }

    public Doctor findByUsername(String username) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username))
                .getId();
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + username));
    }

    public DoctorResponse searchByAmka(String amka) {
        String amkaHash = AmkaHashUtil.hashAmka(amka);
        return toResponse(doctorRepository.findByAmkaHash(amkaHash)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with AMKA: " + amka + " not found.")));
    }

    public List<DoctorResponse> searchByName(String name) {
        return doctorRepository.searchByName(name).stream().map(this::toResponse).toList();
    }

    public void verifyDoctorOwnsChild(Doctor doctor, Long childId) {
        boolean assigned = doctor.getChildren().stream().anyMatch(c -> c.getId().equals(childId));
        if (!assigned) {
            throw new AccessDeniedException("Doctor is not assigned to this child");
        }
    }

    public DoctorResponse toResponse(Doctor d) {
        return new DoctorResponse(d.getId(), d.getFirstName(), d.getLastName(), d.getAmka(),
                d.getEmail(), d.getPhoneNumber(), d.getSpecialization(), d.getLicenseNumber(), d.getAddress());
    }
}
