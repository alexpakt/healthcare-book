package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.response.ChildResponse;
import com.healthcare.childrecords.dto.response.DoctorResponse;
import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.entity.Parent;
import com.healthcare.childrecords.entity.Role;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.ChildRepository;
import com.healthcare.childrecords.repository.UserRepository;
import com.healthcare.childrecords.util.AmkaHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildService {

    private final ChildRepository childRepository;
    private final ParentService parentService;
    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final ChildAccessGuard childAccessGuard;

    @Transactional
    public ChildResponse create(String username, ChildRequest request) {
        Parent parent = parentService.findByUsername(username);
        Child child = Child.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .amka(request.amka())
                .amkaHash(AmkaHashUtil.hashAmka(request.amka()))
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .bloodType(request.bloodType())
                .parent(parent)
                .build();
        return toResponse(childRepository.save(child));
    }

    public List<ChildResponse> getMyChildren(String username) {
        Role role = userRepository.findByUsername(username).orElseThrow().getRole();
        if (role == Role.ROLE_PARENT) {
            Parent parent = parentService.findByUsername(username);
            return childRepository.findByParentId(parent.getId()).stream().map(this::toResponse).toList();
        } else {
            Doctor doctor = doctorService.findByUsername(username);
            return childRepository.findByDoctorId(doctor.getId()).stream().map(this::toResponse).toList();
        }
    }

    public ChildResponse getById(String username, Long childId) {
        Child child = findById(childId);
        childAccessGuard.verifyAccess(username, child);
        return toResponse(child);
    }

    public List<DoctorResponse> getDoctors(String username, Long childId) {
        Child child = findById(childId);
        childAccessGuard.verifyAccess(username, child);
        return child.getDoctors().stream().map(doctorService::toResponse).toList();
    }

    @Transactional
    public ChildResponse update(String username, Long childId, ChildRequest request) {
        Child child = findById(childId);
        childAccessGuard.verifyParentOwnership(username, child);
        child.setFirstName(request.firstName());
        child.setLastName(request.lastName());
        child.setDateOfBirth(request.dateOfBirth());
        child.setGender(request.gender());
        child.setBloodType(request.bloodType());
        return toResponse(childRepository.save(child));
    }

    @Transactional
    public void delete(String username, Long childId) {
        Child child = findById(childId);
        childAccessGuard.verifyParentOwnership(username, child);
        childRepository.delete(child);
    }

    @Transactional
    public ChildResponse assignDoctor(String username, Long childId, Long doctorId) {
        Child child = findById(childId);
        childAccessGuard.verifyParentOwnership(username, child);
        Doctor doctor = doctorService.findById(doctorId);
        if (!child.getDoctors().contains(doctor)) {
            child.getDoctors().add(doctor);
            childRepository.save(child);
        }
        return toResponse(child);
    }

    @Transactional
    public ChildResponse removeDoctor(String username, Long childId, Long doctorId) {
        Child child = findById(childId);
        childAccessGuard.verifyParentOwnership(username, child);
        child.getDoctors().removeIf(d -> d.getId().equals(doctorId));
        return toResponse(childRepository.save(child));
    }

    @Transactional
    public void ensureDoctorAssigned(Child child, Doctor doctor) {
        if (!child.getDoctors().contains(doctor)) {
            child.getDoctors().add(doctor);
            childRepository.save(child);
        }
    }

    public ChildResponse searchByAmka(String amka) {
        String amkaHash = AmkaHashUtil.hashAmka(amka);
        Child child = childRepository.findByAmkaHash(amkaHash)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with AMKA: " + amka));
        return toResponse(child);
    }

    public Child findById(Long id) {
        return childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + id));
    }

    private ChildResponse toResponse(Child c) {
        Parent parent = c.getParent();
        return new ChildResponse(c.getId(), c.getFirstName(), c.getLastName(), c.getAmka(),
                c.getDateOfBirth(), c.getGender(), c.getBloodType(), parent.getId(),
                parent.getFirstName(), parent.getLastName());
    }
}
