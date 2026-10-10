package com.healthcare.childrecords.service;

import com.healthcare.childrecords.entity.Child;
import com.healthcare.childrecords.entity.Doctor;
import com.healthcare.childrecords.entity.Parent;
import com.healthcare.childrecords.entity.Role;
import com.healthcare.childrecords.exception.AccessDeniedException;
import com.healthcare.childrecords.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Central place for deciding whether the authenticated user may act on a child's data.
 * Parents may access children they own; doctors may access children they are assigned to.
 */
@Component
@RequiredArgsConstructor
public class ChildAccessGuard {

    private final ParentService parentService;
    private final DoctorService doctorService;
    private final UserRepository userRepository;

    /** Read/write access to a child's clinical data: owning parent, or an assigned doctor. */
    public void verifyAccess(String username, Child child) {
        Role role = userRepository.findByUsername(username).orElseThrow().getRole();
        if (role == Role.ROLE_PARENT) {
            Parent parent = parentService.findByUsername(username);
            if (!child.getParent().getId().equals(parent.getId())) {
                throw new AccessDeniedException("Access denied to this child's data");
            }
        } else {
            Doctor doctor = doctorService.findByUsername(username);
            boolean assigned = child.getDoctors().stream().anyMatch(d -> d.getId().equals(doctor.getId()));
            if (!assigned) {
                throw new AccessDeniedException("Doctor is not assigned to this child");
            }
        }
    }

    /** Administrative actions that only the owning parent may perform. */
    public void verifyParentOwnership(String username, Child child) {
        Parent parent = parentService.findByUsername(username);
        if (!child.getParent().getId().equals(parent.getId())) {
            throw new AccessDeniedException("Parent does not own this child");
        }
    }
}
